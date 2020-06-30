package no.nav.syfo.pdfgenerering

import no.nav.syfo.LocalApplication
import no.nav.syfo.pdf.PdfFabrikk
import no.nav.syfo.util.PropertyUtil
import no.nav.syfo.util.ToggleUtil
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class PdfFabrikkTest {
    private val xml = """<oppfoelgingsdialogXML>
    <arbeidsgivernavn>Are Arbeidsgiver</arbeidsgivernavn>
    <virksomhetsnavn>Org Organisasjon</virksomhetsnavn>
    <sykmeldtnavn>Test Testesen</sykmeldtnavn>
    <gyldigfra>08.08.2017</gyldigfra>
    <gyldigtil>28.08.2017</gyldigtil>
    <evalueres>31.08.2017</evalueres>
    <visAdvarsel>false</visAdvarsel>
    <sykmeldtFnr>1010101010101</sykmeldtFnr>
    <sykmeldtTlf>+4799999999</sykmeldtTlf>
    <sykmeldtEpost>test@testesen.no</sykmeldtEpost>
    <arbeidsgiverOrgnr>123456789</arbeidsgiverOrgnr>
    <arbeidsgiverTlf>12345678</arbeidsgiverTlf>
    <arbeidsgiverEpost>test@nav.no</arbeidsgiverEpost>
    <godkjentAv>Test Testesen</godkjentAv>
    <opprettetAv>Test Testesen</opprettetAv>
    <opprettetDato>31.07.2017</opprettetDato>
    <godkjentDato>31.07.2017</godkjentDato>
</oppfoelgingsdialogXML>"""

    @Before
    fun setup() {
        System.setProperty(PropertyUtil.ENVIRONMENT_NAME, ToggleUtil.ENVIRONMENT_MODE.dev.name)
    }

    @Test
    @Throws(Exception::class)
    fun lagPdf() {
        PdfFabrikk.tilPdf(xml)
    }
}
