package no.nav.syfo.service;

import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PdfService {

    private static final Logger log = getLogger(PdfService.class);

    private OppfolgingsplanDAO oppfolgingsplanDAO;

    private OppfolgingsplanService oppfolgingsplanService;

    private DokumentDAO dokumentDAO;

    private GodkjentplanDAO godkjentplanDAO;

    private Metrikk metrikk;

    private float pngDpiResolution = 150.0f;

    private ImageType pngImageType = ImageType.RGB;

    @Inject
    public PdfService(
            OppfolgingsplanDAO oppfolgingsplanDAO,
            OppfolgingsplanService oppfolgingsplanService,
            DokumentDAO dokumentDAO,
            GodkjentplanDAO godkjentplanDAO,
            Metrikk metrikk
    ) {
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.oppfolgingsplanService = oppfolgingsplanService;
        this.dokumentDAO = dokumentDAO;
        this.godkjentplanDAO = godkjentplanDAO;
        this.metrikk = metrikk;
    }

    public byte[] hentPdf(long oppfolgingsplanId, String innloggetFnr) {
        if (!oppfolgingsplanService.harBrukerTilgangTilDialog(oppfolgingsplanId, innloggetFnr)) {
            throw new ForbiddenException("Ikke tilgang");
        }
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        metrikk.tellAntallDagerSiden(oppfolgingsplan.opprettet, "antallDagerFraOpprettetTilPdf");
        Optional<GodkjentPlan> godkjentPlanOptional = godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfolgingsplanId);
        if (godkjentPlanOptional.isPresent()) {
            String dokumentUuid = godkjentPlanOptional.get().dokumentUuid;
            return dokumentDAO.hent(dokumentUuid);
        } else {
            metrikk.tellHendelse("hent_pdf_missing_godkjentplan");
            log.error("Did not find PDF due to missing GodkjentPlan for plan {}", oppfolgingsplanId);
            throw new RuntimeException("Did not find PDF due to missing GodkjentPlan for plan");
        }
    }

    public byte[] hentPdfTilAltinn(Oppfolgingsplan oppfolgingsplan) {
        metrikk.tellAntallDagerSiden(oppfolgingsplan.opprettet, "antallDagerFraOpprettetTilPdf");

        GodkjentPlan godkjentPlan = oppfolgingsplan.godkjentPlan.orElseThrow(() ->
                throwOppfoelgingsplanUtenGodkjenPlan(oppfolgingsplan)
        );

        return dokumentDAO.hent(godkjentPlan.dokumentUuid);
    }
    public byte[] hentPdfTilArkivporten(Oppfolgingsplan oppfolgingsplan) {
        // skip the metric here for now as it is already counted in hentPdfTilAltinn
        // metrikk.tellAntallDagerSiden(oppfolgingsplan.opprettet, "antallDagerFraOpprettetTilPdf");

        GodkjentPlan godkjentPlan = oppfolgingsplan.godkjentPlan.orElseThrow(() ->
                throwOppfoelgingsplanUtenGodkjenPlan(oppfolgingsplan)
        );

        return dokumentDAO.hent(godkjentPlan.dokumentUuid);
    }

    public byte[] pdf2image(byte[] pdfBytes, int side) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = pdfRenderer.renderImageWithDPI(side - 1, pngDpiResolution, pngImageType);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIOUtil.writeImage(image, "png", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Fikk feil ved konvertering fra PDF til PNG");
            throw new RuntimeException("Klarte ikke å konvertere fra PDF til PNG");
        }
    }

    public int hentAntallSiderIDokument(byte[] pdf) {
        InputStream is = new ByteArrayInputStream(pdf);
        try {
            PDDocument document = PDDocument.load(is);
            int antallSider = document.getNumberOfPages();
            document.close();
            return antallSider;
        } catch (IOException e) {
            log.error("Catched IOException when get number of pages of document", e);
            return 1;
        }
    }

    private RuntimeException throwOppfoelgingsplanUtenGodkjenPlan(Oppfolgingsplan oppfolgingsplan) {
        log.error("Oppfoelgingsplan med id {} har ikke godkjentPlan", oppfolgingsplan.id);
        return new RuntimeException("Oppfoelgingsplan har ikke godkjentPlan");
    }
}
