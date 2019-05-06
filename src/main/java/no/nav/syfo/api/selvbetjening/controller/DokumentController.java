package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.PdfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static org.springframework.http.MediaType.APPLICATION_PDF;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/dokument/{oppfolgingsplanId}/ekstern")
public class DokumentController {

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
}
