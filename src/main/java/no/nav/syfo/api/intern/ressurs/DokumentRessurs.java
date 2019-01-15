package no.nav.syfo.api.intern.ressurs;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.service.DokumentService;
import no.nav.syfo.service.PdfService;
import no.nav.syfo.service.TilgangsKontroll;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.getProperty;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ok;
import static no.nav.syfo.mockdata.MockData.mockPdf;
import static no.nav.syfo.mockdata.MockData.mockPdfBytes;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@Path("/dokument/{oppfoelgingsdialogId}")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class DokumentRessurs {

    private static final Logger LOG = getLogger(DokumentRessurs.class);

    @Inject
    private DokumentService dokumentService;
    @Inject
    private PdfService pdfService;
    @Inject
    private GodkjentplanDAO godkjentplanDAO;
    @Inject
    private TilgangsKontroll tilgangsKontroll;

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
            LOG.error("Fikk IndexOutOfBoundsException ved henting av side {} for oppfoelgingsplan {} ", side, oppfoelgingsdialogId);
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
