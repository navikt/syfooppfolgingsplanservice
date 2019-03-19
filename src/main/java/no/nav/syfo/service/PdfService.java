package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.repository.dao.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.awt.image.BufferedImage;
import java.io.*;

import static no.nav.syfo.util.MetricsUtil.reportAntallDagerSiden;

@Slf4j
public class PdfService {

    @Inject
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @Inject
    private OppfoelgingsdialogService oppfoelgingsdialogService;
    @Inject
    private DokumentDAO dokumentDAO;
    @Inject
    private GodkjentplanDAO godkjentplanDAO;

    public byte[] hentPdf(long oppfoelgingsdialogId, String fnr) {
        if (!oppfoelgingsdialogService.harBrukerTilgangTilDialog(oppfoelgingsdialogId, fnr)) {
            throw new ForbiddenException("Ikke tilgang");
        }
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        reportAntallDagerSiden(oppfoelgingsdialog.opprettet, "antallDagerFraOpprettetTilPdf");
        String dokumentUuid = godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(oppfoelgingsdialogId).get().dokumentUuid;

        return dokumentDAO.hent(dokumentUuid);
    }

    public byte[] hentPdfTilAltinn(Oppfoelgingsdialog oppfoelgingsdialog) {
        reportAntallDagerSiden(oppfoelgingsdialog.opprettet, "antallDagerFraOpprettetTilPdf");

        GodkjentPlan godkjentPlan = oppfoelgingsdialog.godkjentPlan.orElseThrow(() ->
                throwOppfoelgingsplanUtenGodkjenPlan(oppfoelgingsdialog)
        );

        return dokumentDAO.hent(godkjentPlan.dokumentUuid);
    }

    public byte[] pdf2image(byte[] pdfBytes, int side) throws IOException {
        PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes));
        PDFRenderer pdfRenderer = new PDFRenderer(document);


        BufferedImage image = pdfRenderer.renderImageWithDPI(side - 1, 300, ImageType.RGB);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIOUtil.writeImage(image, "png", byteArrayOutputStream);
        document.close();
        return byteArrayOutputStream.toByteArray();
    }

    private RuntimeException throwOppfoelgingsplanUtenGodkjenPlan(Oppfoelgingsdialog oppfoelgingsdialog) {
        log.error("Oppfoelgingsplan med id {} har ikke godkjentPlan", oppfoelgingsdialog.id);
        return new RuntimeException("Oppfoelgingsplan har ikke godkjentPlan");
    }
}
