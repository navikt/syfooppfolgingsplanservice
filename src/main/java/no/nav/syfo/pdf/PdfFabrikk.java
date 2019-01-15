package no.nav.syfo.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.DefaultPDFCreationListener;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import static java.util.Optional.ofNullable;
import static no.nav.syfo.util.ToggleUtil.kjorerLokalt;
import static no.nav.syfo.util.XmlUtil.xmlTilHtml;
import static org.apache.commons.io.IOUtils.toByteArray;

public class PdfFabrikk {

    private static final Logger LOG = LoggerFactory.getLogger(PdfFabrikk.class);

    public static byte[] tilPdf(String xml) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ByteArrayOutputStream stamperOs = new ByteArrayOutputStream();
        try {
            ITextRenderer renderer = new ITextRenderer() {

            };
            renderer.setListener(new DefaultPDFCreationListener() {
                @Override
                public void preOpen(ITextRenderer iTextRenderer) {
                    iTextRenderer.getWriter().setPDFXConformance(PdfWriter.PDFA1A);
                    iTextRenderer.getWriter().createXmpMetadata();

                    super.preOpen(iTextRenderer);
                }
            });

            String path;
            if (kjorerLokalt()) {
                path = "file://" + PdfFabrikk.class.getClassLoader().getResource("pdf").getPath() + '/';
            } else {
                path = ofNullable(PdfFabrikk.class.getClassLoader().getResource("pdf"))
                        .map(URL::toExternalForm)
                        .orElseThrow(() -> new RuntimeException("Feil ved produering av PDF: Finner ikke pdf-katalogen"));
            }
            String html = pakkUtCData(tilHtml(xml));

            renderer.setDocumentFromString(html, path);
            leggTilFonter(renderer);

            renderer.layout();
            renderer.setPDFVersion(PdfWriter.VERSION_1_7);
            renderer.createPDF(os, false, 0);
            renderer.getWriter().setOutputIntents("Custom", "PDF/A", "http://www.color.org", "AdobeRGB1998",
                    toByteArray(PdfFabrikk.class.getClassLoader().getResourceAsStream("AdobeRGB1998.icc")));
            renderer.finishPDF();

            PdfReader reader = new PdfReader(os.toByteArray());
            PdfStamper stamper = new PdfStamper(reader, stamperOs);

            stamper.close();
            reader.close();
            stamperOs.close();
            os.close();
        } catch (DocumentException | IOException e) {
            LOG.error("Feil i konvertering av xml til pdf", e);
            throw new RuntimeException("Kunne ikke generere PDF", e);
        }
        return stamperOs.toByteArray();
    }

    private static void leggTilFonter(ITextRenderer renderer) throws DocumentException, IOException {
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-Black.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-BlackIt.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-Bold.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-BoldIt.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-ExtraLight.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-ExtraLightIt.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-It.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-Light.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-LightIt.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-Regular.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-Semibold.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
        renderer.getFontResolver().addFont("/fonts/sourcesans/SourceSansPro-SemiboldIt.otf", "SourceSans", BaseFont.WINANSI, BaseFont.EMBEDDED, null);
    }

    private static String pakkUtCData(String html) {
        return html.replaceAll("<!\\[CDATA\\[", "").replaceAll("]]>", "");
    }

    public static String tilHtml(String xml) {
        String html;
        try {
            html = xmlTilHtml(xml, PdfFabrikk.class.getClassLoader().getResourceAsStream("oppfoelgingsdialog.xsl"));
        } catch (JAXBException | FileNotFoundException | TransformerException e) {
            LOG.error("Feil i konvertering av xml til HTML", e);
            throw new RuntimeException();
        }
        return html;
    }
}
