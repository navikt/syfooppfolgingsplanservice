package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.awt.image.BufferedImage;
import java.io.*;

@Slf4j
@Service
public class PdfService {

    private OppfoelingsdialogDAO oppfoelingsdialogDAO;

    private OppfolgingsplanService oppfolgingsplanService;

    private DokumentDAO dokumentDAO;

    private GodkjentplanDAO godkjentplanDAO;

    private Metrikk metrikk;

    private float pngDpiResolution = 150.0f;

    private ImageType pngImageType = ImageType.RGB;

    @Inject
    public PdfService(
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            OppfolgingsplanService oppfolgingsplanService,
            DokumentDAO dokumentDAO,
            GodkjentplanDAO godkjentplanDAO,
            Metrikk metrikk
    ) {
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.oppfolgingsplanService = oppfolgingsplanService;
        this.dokumentDAO = dokumentDAO;
        this.godkjentplanDAO = godkjentplanDAO;
        this.metrikk = metrikk;
    }

    public byte[] hentPdf(long oppfolgingsplanId, String innloggetFnr) {
        if (!oppfolgingsplanService.harBrukerTilgangTilDialog(oppfolgingsplanId, innloggetFnr)) {
            throw new ForbiddenException("Ikke tilgang");
        }
        Oppfoelgingsdialog oppfolgingsplan = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        metrikk.tellAntallDagerSiden(oppfolgingsplan.opprettet, "antallDagerFraOpprettetTilPdf");
        String dokumentUuid = godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfolgingsplanId).get().dokumentUuid;

        return dokumentDAO.hent(dokumentUuid);
    }

    public byte[] hentPdfTilAltinn(Oppfoelgingsdialog oppfoelgingsdialog) {
        metrikk.tellAntallDagerSiden(oppfoelgingsdialog.opprettet, "antallDagerFraOpprettetTilPdf");

        GodkjentPlan godkjentPlan = oppfoelgingsdialog.godkjentPlan.orElseThrow(() ->
                throwOppfoelgingsplanUtenGodkjenPlan(oppfoelgingsdialog)
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
            return 1;
        }
    }

    private RuntimeException throwOppfoelgingsplanUtenGodkjenPlan(Oppfoelgingsdialog oppfoelgingsdialog) {
        log.error("Oppfoelgingsplan med id {} har ikke godkjentPlan", oppfoelgingsdialog.id);
        return new RuntimeException("Oppfoelgingsplan har ikke godkjentPlan");
    }
}
