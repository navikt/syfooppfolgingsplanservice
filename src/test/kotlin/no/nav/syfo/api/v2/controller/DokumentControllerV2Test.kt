package no.nav.syfo.api.v2.controller


import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.PdfService
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class DokumentControllerV2Test : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var dokumentController: DokumentControllerV2

    @MockBean
    lateinit var pdfService: PdfService

    @MockBean
    lateinit var metrikk: Metrikk

    @Test
    fun hent_pdf_som_bruker() {
        tokenValidationTestUtil.logInAsUser(ARBEIDSTAKER_FNR)
        val oppfolgingsplanId = 1L
        val pdf = ByteArray(10)
        `when`(pdfService.hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(pdf)
        val response = dokumentController.hentPdf(oppfolgingsplanId)
        verify(pdfService).hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        assertEquals(200, response.statusCode.value().toLong())
        assertEquals(pdf, response.body)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_hent_pdf() {
        dokumentController.hentPdf(1)
    }
}
