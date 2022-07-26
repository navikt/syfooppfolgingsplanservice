package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.PdfService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.IMAGE_PNG;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/dokument/{oppfolgingsplanId}/ekstern")
public class DokumentController {

    private static final Logger log = getLogger(DokumentController.class);

    @Value("${nais.cluster.name}")
    private String envName;
    private final TokenValidationContextHolder contextHolder;
    private final PdfService pdfService;
    private final Metrikk metrikk;

    @Inject
    public DokumentController(
            TokenValidationContextHolder contextHolder,
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

}
