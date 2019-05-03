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

    private OppfoelgingsdialogService oppfoelgingsdialogService;

    private DokumentDAO dokumentDAO;

    private GodkjentplanDAO godkjentplanDAO;

    private Metrikk metrikk;

    @Inject
    public PdfService(
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            OppfoelgingsdialogService oppfoelgingsdialogService,
            DokumentDAO dokumentDAO,
            GodkjentplanDAO godkjentplanDAO,
            Metrikk metrikk
    ) {
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.oppfoelgingsdialogService = oppfoelgingsdialogService;
        this.dokumentDAO = dokumentDAO;
        this.godkjentplanDAO = godkjentplanDAO;
        this.metrikk = metrikk;
    }

    public byte[] hentPdf(long oppfolgingsplanId, String innloggetFnr) {
        if (!oppfoelgingsdialogService.harBrukerTilgangTilDialog(oppfolgingsplanId, innloggetFnr)) {
            throw new ForbiddenException("Ikke tilgang");
        }
        Oppfoelgingsdialog oppfolgingsplan = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfolgingsplanId);
        metrikk.tellAntallDagerSiden(oppfolgingsplan.opprettet, "antallDagerFraOpprettetTilPdf");
        String dokumentUuid = godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(oppfolgingsplanId).get().dokumentUuid;

        return dokumentDAO.hent(dokumentUuid);
    }

    public byte[] hentPdfTilAltinn(Oppfoelgingsdialog oppfoelgingsdialog) {
        metrikk.tellAntallDagerSiden(oppfoelgingsdialog.opprettet, "antallDagerFraOpprettetTilPdf");

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
