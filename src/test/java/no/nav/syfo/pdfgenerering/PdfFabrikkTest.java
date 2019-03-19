package no.nav.syfo.pdfgenerering;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.pdf.PdfFabrikk;
import no.nav.syfo.util.ToggleUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static no.nav.syfo.util.PropertyUtil.ENVIRONMENT_NAME;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class PdfFabrikkTest {

    private String xml = "<oppfoelgingsdialogXML>\n" +
            "    <arbeidsgivernavn>Are Arbeidsgiver</arbeidsgivernavn>\n" +
            "    <virksomhetsnavn>Org Organisasjon</virksomhetsnavn>\n" +
            "    <sykmeldtnavn>Test Testesen</sykmeldtnavn>\n" +
            "    <gyldigfra>08.08.2017</gyldigfra>\n" +
            "    <gyldigtil>28.08.2017</gyldigtil>\n" +
            "    <evalueres>31.08.2017</evalueres>\n" +
            "    <visAdvarsel>false</visAdvarsel>\n" +
            "    <sykmeldtFnr>1010101010101</sykmeldtFnr>\n" +
            "    <sykmeldtTlf>+4799999999</sykmeldtTlf>\n" +
            "    <sykmeldtEpost>test@testesen.no</sykmeldtEpost>\n" +
            "    <arbeidsgiverOrgnr>123456789</arbeidsgiverOrgnr>\n" +
            "    <arbeidsgiverTlf>12345678</arbeidsgiverTlf>\n" +
            "    <arbeidsgiverEpost>test@nav.no</arbeidsgiverEpost>\n" +
            "    <godkjentAv>Test Testesen</godkjentAv>\n" +
            "    <opprettetAv>Test Testesen</opprettetAv>\n" +
            "    <opprettetDato>31.07.2017</opprettetDato>\n" +
            "    <godkjentDato>31.07.2017</godkjentDato>\n" +
            "</oppfoelgingsdialogXML>";


    @Before
    public void setup() {
        System.setProperty(ENVIRONMENT_NAME, ToggleUtil.ENVIRONMENT_MODE.dev.name());
    }

    @Test
    public void lagPdf() throws Exception {
        PdfFabrikk.tilPdf(xml);
    }
}
