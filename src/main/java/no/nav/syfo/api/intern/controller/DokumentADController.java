package no.nav.syfo.api.intern.controller;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.io.IOException;

import static no.nav.syfo.oidc.OIDCIssuer.AZURE;
import static org.springframework.http.MediaType.*;

@Slf4j
@RestController
@ProtectedWithClaims(issuer = AZURE)
@RequestMapping(value = "/api/internad/dokument/{oppfoelgingsdialogId}")
public class DokumentADController {

    private DokumentService dokumentService;
    private GodkjentplanDAO godkjentplanDAO;
    private PdfService pdfService;
    private VeilederTilgangService veilederTilgangService;

    @Inject
    public DokumentADController(
            final DokumentService dokumentService,
            final GodkjentplanDAO godkjentplanDAO,
            final PdfService pdfService,
            final VeilederTilgangService veilederTilgangService
    ) {
        this.dokumentService = dokumentService;
        this.godkjentplanDAO = godkjentplanDAO;
        this.pdfService = pdfService;
        this.veilederTilgangService = veilederTilgangService;
    }

    @GetMapping
    @RequestMapping(value = "/side/{side}")
    public ResponseEntity bilde(@PathVariable("oppfoelgingsdialogId") long oppfoelgingsdialogId, @PathVariable("side") int side) throws IOException {
        veilederTilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilTjenestenViaAzure();

        try {
            byte[] pdf = getPdf(oppfoelgingsdialogId);
            return ResponseEntity.ok()
                    .contentType(IMAGE_PNG)
                    .body(pdfService.pdf2image(pdf, side));
        } catch (IndexOutOfBoundsException e) {
            log.error("Fikk IndexOutOfBoundsException ved henting av side {} for oppfoelgingsplan {} ", side, oppfoelgingsdialogId);
            throw e;
        }
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/dokumentinfo")
    public Dokumentinfo dokumentinfo(@PathVariable("oppfoelgingsdialogId") long oppfoelgingsdialogId) {
        veilederTilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilTjenestenViaAzure();

        byte[] pdf = getPdf(oppfoelgingsdialogId);
        return new Dokumentinfo().antallSider(pdfService.hentAntallSiderIDokument(pdf));
    }

    @Data
    @Accessors(fluent = true)
    public class Dokumentinfo {

        public int antallSider;
    }

    @GetMapping
    public ResponseEntity dokument(@PathVariable("oppfoelgingsdialogId") long oppfoelgingsdialogId) {
        veilederTilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilTjenestenViaAzure();

        byte[] pdf = getPdf(oppfoelgingsdialogId);
        return ResponseEntity.ok()
                .contentType(APPLICATION_PDF)
                .body(pdf);
    }

    private byte[] getPdf(long oppfoelgingsdialogId) {
        return godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfoelgingsdialogId)
                .map(GodkjentPlan::dokumentUuid)
                .map(dokumentService::hentDokument)
                .orElseThrow(() -> new NotFoundException("Klarte ikke å hente ut godkjent plan for oppfølgingsdialogId " + oppfoelgingsdialogId));
    }
}
