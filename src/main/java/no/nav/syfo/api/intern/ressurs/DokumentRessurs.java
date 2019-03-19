package no.nav.syfo.api.intern.ressurs;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.service.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.*;

import static java.lang.System.getProperty;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ok;
import static no.nav.syfo.mockdata.MockData.mockPdf;
import static no.nav.syfo.mockdata.MockData.mockPdfBytes;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;

@Slf4j
@Component
@Path("/dokument/{oppfoelgingsdialogId}")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class DokumentRessurs {

    private DokumentService dokumentService;
    private GodkjentplanDAO godkjentplanDAO;
    private PdfService pdfService;
    private TilgangsKontroll tilgangsKontroll;

    @Inject
    public DokumentRessurs(
            final DokumentService dokumentService,
            final GodkjentplanDAO godkjentplanDAO,
            final PdfService pdfService,
            final TilgangsKontroll tilgangsKontroll
    ) {
        this.dokumentService = dokumentService;
        this.godkjentplanDAO = godkjentplanDAO;
        this.pdfService = pdfService;
        this.tilgangsKontroll = tilgangsKontroll;
    }

    @GET
    @Path("/side/{side}")
    public Response bilde(@PathParam("oppfoelgingsdialogId") long oppfoelgingsdialogId, @PathParam("side") int side) throws IOException {
        tilgangsKontroll.sjekkTilgangTilTjenesten();

        if ("true".equals(getProperty(LOCAL_MOCK))) {
            return Response.ok()
                    .type("image/png")
                    .entity(pdfService.pdf2image(mockPdfBytes(), side))
                    .build();
        }
        try {
            byte[] pdf = getPdf(oppfoelgingsdialogId);
            return Response.ok()
                    .type("image/png")
                    .entity(pdfService.pdf2image(pdf, side))
                    .build();
        } catch (IndexOutOfBoundsException e) {
            log.error("Fikk IndexOutOfBoundsException ved henting av side {} for oppfoelgingsplan {} ", side, oppfoelgingsdialogId);
            throw e;
        }
    }

    @GET
    @Path("/dokumentinfo")
    public Dokumentinfo dokumentinfo(@PathParam("oppfoelgingsdialogId") long oppfoelgingsdialogId) {
        tilgangsKontroll.sjekkTilgangTilTjenesten();

        if ("true".equals(getProperty(LOCAL_MOCK))) {
            return new Dokumentinfo().antallSider(2);
        }
        byte[] pdf = getPdf(oppfoelgingsdialogId);
        return new Dokumentinfo().antallSider(hentAntallSiderIDokument(pdf));
    }

    @Data
    @Accessors(fluent = true)
    public class Dokumentinfo {

        public int antallSider;
    }

    @GET
    public Response dokument(@PathParam("oppfoelgingsdialogId") long oppfoelgingsdialogId) {
        tilgangsKontroll.sjekkTilgangTilTjenesten();
        if ("true".equals(getProperty(LOCAL_MOCK))) {
            return mockPdf();
        }
        byte[] pdf = getPdf(oppfoelgingsdialogId);
        return ok()
                .type("application/pdf")
                .entity(pdf)
                .build();
    }

    private byte[] getPdf(long oppfoelgingsdialogId) {
        return godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(oppfoelgingsdialogId)
                .map(GodkjentPlan::dokumentUuid)
                .map(dokumentService::hentDokument)
                .orElseThrow(() -> new NotFoundException("Klarte ikke å hente ut godkjent plan for oppfølgingsdialogId " + oppfoelgingsdialogId));
    }

    private int hentAntallSiderIDokument(byte[] pdf) {
        InputStream is = new ByteArrayInputStream(pdf);
        try {
            return PDDocument.load(is).getNumberOfPages();
        } catch (IOException e) {
            return 1;
        }
    }

}
