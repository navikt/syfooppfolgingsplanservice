package no.nav.syfo.dokarkiv;

import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.sts.StsConsumer;
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

@Service
public class DokArkivConsumer {
    //TODO: Fix syfonais
    private static final String JOURNALPOSTAPI_PATH = "/rest/journalpostapi/v1/journalpost";

    private final RestTemplate restTemplate;
    private final String url;
    private final StsConsumer stsConsumer;

    @Inject
    public DokArkivConsumer(RestTemplate restTemplate, @Value("${dokarkiv.url}") String url, StsConsumer stsConsumer
    ) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.stsConsumer = stsConsumer;
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

            return response.getBody().journalpostId;
        } catch (RestClientException e){
            //metrics
            //logs
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
                .sakstype("generell_sak");
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
                .filtype("PDF")
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

