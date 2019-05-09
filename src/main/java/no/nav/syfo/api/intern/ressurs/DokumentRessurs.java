package no.nav.syfo.api.intern.ressurs;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.io.IOException;

import static java.lang.System.getProperty;
import static no.nav.syfo.mockdata.MockData.mockPdf;
import static no.nav.syfo.mockdata.MockData.mockPdfBytes;
import static no.nav.syfo.oidc.OIDCIssuer.INTERN;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static org.springframework.http.MediaType.*;

@Slf4j
@RestController
@ProtectedWithClaims(issuer = INTERN)
@RequestMapping(value = "/api/dokument/{oppfoelgingsdialogId}")
public class DokumentRessurs {

    private DokumentService dokumentService;
    private GodkjentplanDAO godkjentplanDAO;
    private PdfService pdfService;
    private VeilederTilgangService veilederTilgangService;

    @Inject
    public DokumentRessurs(
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
        veilederTilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilTjenesten();

        if ("true".equals(getProperty(LOCAL_MOCK))) {
            return ResponseEntity.ok()
                    .contentType(IMAGE_PNG)
                    .body(pdfService.pdf2image(mockPdfBytes(), side));
        }
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
        veilederTilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilTjenesten();

        if ("true".equals(getProperty(LOCAL_MOCK))) {
            return new Dokumentinfo().antallSider(2);
        }
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
        veilederTilgangService.kastExceptionHvisIkkeVeilederHarTilgangTilTjenesten();
        if ("true".equals(getProperty(LOCAL_MOCK))) {
            return mockPdf();
        }
        byte[] pdf = getPdf(oppfoelgingsdialogId);
        return ResponseEntity.ok()
                .contentType(APPLICATION_PDF)
                .body(pdf);
    }

    private byte[] getPdf(long oppfoelgingsdialogId) {
        return godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(oppfoelgingsdialogId)
                .map(GodkjentPlan::dokumentUuid)
                .map(dokumentService::hentDokument)
                .orElseThrow(() -> new NotFoundException("Klarte ikke å hente ut godkjent plan for oppfølgingsdialogId " + oppfoelgingsdialogId));
    }
}
