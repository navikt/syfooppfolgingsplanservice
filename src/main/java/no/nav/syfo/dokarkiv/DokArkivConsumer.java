package no.nav.syfo.dokarkiv;

import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.lps.OppfolgingsplanLPS;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.sts.StsConsumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static no.nav.syfo.util.CredentialUtilKt.bearerHeader;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class DokArkivConsumer {

    private static final Logger log = getLogger(DokArkivConsumer.class);

    private static final String JOURNALPOSTAPI_PATH = "/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true";

    private final RestTemplate restTemplate;
    private final String url;
    private final StsConsumer stsConsumer;
    private final Metrikk metrikk;

    @Inject
    public DokArkivConsumer(
            @Qualifier("scheduler") RestTemplate restTemplate,
            @Value("${dokarkiv.url}") String url,
            StsConsumer stsConsumer,
            Metrikk metrikk
    ) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.stsConsumer = stsConsumer;
        this.metrikk = metrikk;
    }

    public Integer journalforOppfolgingsplan(Oppfolgingsplan oppfolgingsplan, GodkjentPlan godkjentPlan) {
        JournalpostRequest request = createJournalpostRequest(
                oppfolgingsplan.virksomhet.navn,
                oppfolgingsplan.arbeidstaker.fnr,
                godkjentPlan.dokument,
                hentSistEndret(oppfolgingsplan)
        );
        return journalfor(request);
    }

    public Integer journalforOppfolgingsplanLPS(
            OppfolgingsplanLPS oppfolgingsplanLPS,
            String virksomhetsnavn
    ) {
        AvsenderMottaker avsenderMottaker = new AvsenderMottaker()
                .id(oppfolgingsplanLPS.getVirksomhetsnummer())
                .idType("ORGNR")
                .navn(virksomhetsnavn);

        JournalpostRequest request = createJournalpostRequest(
                virksomhetsnavn,
                oppfolgingsplanLPS.getFnr(),
                oppfolgingsplanLPS.getPdf(),
                avsenderMottaker
        );
        return journalfor(request);
    }

    public Integer journalfor(JournalpostRequest request) {
        HttpEntity<JournalpostRequest> entity = createRequestEntity(request);

        try {
            ResponseEntity<JournalpostResponse> response = restTemplate.exchange(
                    this.url + JOURNALPOSTAPI_PATH,
                    HttpMethod.POST,
                    entity,
                    JournalpostResponse.class);
            metrikk.tellHendelse("journalfor_oppfolgingsplan");
            if (!response.getBody().journalpostferdigstilt) {
                log.warn("Journalpost is not ferdigstilt with message: {}", response.getBody().melding);
            }
            return response.getBody().journalpostId;
        } catch (RestClientException e) {
            log.error("Error from DokArkiv " + url + JOURNALPOSTAPI_PATH, e);
            throw e;
        }
    }

    private JournalpostRequest createJournalpostRequest(
            String virksomhetsnavn,
            String arbeidstakerFnr,
            byte[] dokumentPdf,
            AvsenderMottaker avsenderMottaker
    ) {
        String dokumentNavn = format("Oppfølgingsplan %s", virksomhetsnavn);
        Sak sak = new Sak()
                .sakstype("GENERELL_SAK");
        Bruker bruker = new Bruker()
                .id(arbeidstakerFnr)
                .idType("FNR");

        List<Dokument> dokumenter = lagDokumenter(dokumentNavn, dokumentPdf);

        return new JournalpostRequest()
                .tema("OPP")
                .tittel(dokumentNavn)
                .journalfoerendeEnhet(9999)
                .journalpostType("INNGAAENDE")
                .kanal("NAV_NO")
                .sak(sak)
                .avsenderMottaker(avsenderMottaker)
                .bruker(bruker)
                .dokumenter(dokumenter);
    }

    private List<Dokument> lagDokumenter(String dokumentNavn, byte[] dokumentPdf) {

        Dokumentvariant dokumentvariant = new Dokumentvariant()
                .filnavn(dokumentNavn)
                .filtype("PDFA")
                .variantformat("ARKIV")
                .fysiskDokument(dokumentPdf);

        List<Dokumentvariant> dokumentvarianter = new ArrayList<>();
        dokumentvarianter.add(dokumentvariant);

        Dokument dokument = new Dokument()
                .dokumentKategori("ES")
                .brevkode("OPPF_PLA")
                .tittel(dokumentNavn)
                .dokumentvarianter(dokumentvarianter);


        List<Dokument> dokumenter = new ArrayList<>();
        dokumenter.add(dokument);

        return dokumenter;
    }

    private HttpEntity<JournalpostRequest> createRequestEntity(JournalpostRequest request) {
        String stsToken = stsConsumer.token();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, bearerHeader(stsToken));

        return new HttpEntity<>(request, headers);
    }

    private AvsenderMottaker hentSistEndret(Oppfolgingsplan oppfolgingsplan) {
        if (oppfolgingsplan.sistEndretAvAktoerId.equals(oppfolgingsplan.arbeidstaker.aktoerId)) {
            return new AvsenderMottaker()
                    .id(oppfolgingsplan.arbeidstaker.fnr)
                    .idType("FNR")
                    .navn(oppfolgingsplan.arbeidstaker.navn);
        }

        return new AvsenderMottaker()
                .navn(oppfolgingsplan.virksomhet.navn)
                .idType("ORGNR")
                .id(oppfolgingsplan.virksomhet.virksomhetsnummer);
    }
}

