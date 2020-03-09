package no.nav.syfo.dokarkiv;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.sts.StsConsumer;
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
import static no.nav.syfo.util.RestUtils.bearerHeader;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
public class DokArkivConsumer {
    private static final String JOURNALPOSTAPI_PATH = "/rest/journalpostapi/v1/journalpost";

    private final RestTemplate restTemplate;
    private final String url;
    private final StsConsumer stsConsumer;
    private final Metrikk metrikk;


    @Inject
    public DokArkivConsumer(@Qualifier("scheduler") RestTemplate restTemplate, @Value("${dokarkiv.url}") String url, StsConsumer stsConsumer, Metrikk metrikk
    ) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.stsConsumer = stsConsumer;
        this.metrikk = metrikk;
    }

    public Integer journalforOppfolgingsplan(Oppfoelgingsdialog oppfolgingsplan, GodkjentPlan godkjentPlan) {

        JournalpostRequest request = createJournalpostRequest(oppfolgingsplan, godkjentPlan);
        HttpEntity<JournalpostRequest> entity = createRequestEntity(request);

        try {
            ResponseEntity<JournalpostResponse> response = restTemplate.exchange(
                    this.url + JOURNALPOSTAPI_PATH,
                    HttpMethod.POST,
                    entity,
                    JournalpostResponse.class);
            metrikk.tellHendelse("journalfor_oppfolgingsplan");
            return response.getBody().journalpostId;
        } catch (RestClientException e){
            log.error("Error from DokArkiv " + url + JOURNALPOSTAPI_PATH, e);
            throw e;
        }
    }

    private AvsenderMottaker hentSistEndret(Oppfoelgingsdialog oppfolgingsplan) {
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

    private JournalpostRequest createJournalpostRequest(Oppfoelgingsdialog oppfolgingsplan, GodkjentPlan godkjentPlan) {
        String dokumentNavn = format("Oppfølgingsplan %s", oppfolgingsplan.virksomhet.navn);
        Sak sak = new Sak()
                .sakstype("GENERELL_SAK");
        Bruker bruker = new Bruker()
                .id(oppfolgingsplan.arbeidstaker.fnr)
                .idType("FNR");

        List<Dokument> dokumenter = lagDokumenter(dokumentNavn, godkjentPlan);

        return new JournalpostRequest()
                .tema("OPP")
                .tittel(dokumentNavn)
                .journalfoerendeEnhet(9999)
                .journalpostType("INNGAAENDE")
                .kanal("NAV_NO")
                .sak(sak)
                .avsenderMottaker(hentSistEndret(oppfolgingsplan))
                .bruker(bruker)
                .dokumenter(dokumenter);
    }

    private List<Dokument> lagDokumenter(String dokumentNavn, GodkjentPlan godkjentPlan) {

        Dokumentvariant dokumentvariant = new Dokumentvariant()
                .filnavn(dokumentNavn)
                .filtype("PDFA")
                .variantformat("ARKIV")
                .fysiskDokument(godkjentPlan.dokument);

        List<Dokumentvariant> dokumentvarianter = new ArrayList<>();
        dokumentvarianter.add(dokumentvariant);

        Dokument dokument =  new Dokument()
                .dokumentKategori("ES")
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
};

