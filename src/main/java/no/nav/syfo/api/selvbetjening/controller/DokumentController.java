package no.nav.syfo.api.selvbetjening.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.PdfService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.IMAGE_PNG;

@Slf4j
@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/dokument/{oppfolgingsplanId}/ekstern")
public class DokumentController {

    @Value("${nais.cluster.name}")
    private String envName;
    private final OIDCRequestContextHolder contextHolder;
    private final PdfService pdfService;
    private final Metrikk metrikk;

    @Inject
    public DokumentController(
            OIDCRequestContextHolder contextHolder,
            PdfService pdfService,
            Metrikk metrikk
    ) {
        this.contextHolder = contextHolder;
        this.pdfService = pdfService;
        this.metrikk = metrikk;
    }

    @GetMapping
    public ResponseEntity hentPdf(@PathVariable("oppfolgingsplanId") Long oppfolgingsplanId) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        byte[] pdf = pdfService.hentPdf(oppfolgingsplanId, innloggetIdent);

        metrikk.tellHendelse("hent_pdf");

        return ResponseEntity.ok()
                .contentType(APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping
    @RequestMapping(value = "/side/{side}")
    public ResponseEntity hentSidebilde(
            @PathVariable("oppfolgingsplanId") Long oppfolgingsplanId,
            @PathVariable("side") Long side
    ) throws IOException {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        byte[] pdf = pdfService.hentPdf(oppfolgingsplanId, innloggetIdent);

        metrikk.tellHendelse("hent_sidebilde");
        try {
            return ResponseEntity.ok()
                    .contentType(IMAGE_PNG)
                    .body(pdfService.pdf2image(pdf, side.intValue()));
        } catch (IndexOutOfBoundsException e) {
            log.error("Fikk IndexOutOfBoundsException ved henting av side {} for oppfoelgingsplan", side);
            throw e;
        }
    }

    @GetMapping(path = "/pdfurler")
    public List<String> hentPdfurler(@PathVariable("oppfolgingsplanId") Long oppfolgingsplanId) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        byte[] pdf = pdfService.hentPdf(oppfolgingsplanId, innloggetIdent);

        int antallSiderIDokument = pdfService.hentAntallSiderIDokument(pdf);


        String sideBaseUrl = hentSyfoapiUrl(envName) + "/syfooppfolgingsplanservice/api/dokument/" + oppfolgingsplanId + "/side/";

        List<String> pdfurler = new ArrayList<>();
        for (int i = 1; i < antallSiderIDokument + 1; i++) {
            pdfurler.add(sideBaseUrl + i);
        }

        metrikk.tellHendelse("hent_pdfurler");

        return pdfurler;
    }

    String hentSyfoapiUrl(String env) {
        boolean erDev = env.contains("dev-fss");

        String envTekst = erDev ? "-q" : "";
        return "https://syfoapi" + envTekst + ".nav.no";
    }
}
