package no.nav.syfo.api.v2.controller


import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.PdfService
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class DokumentControllerV2Test : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var dokumentController: DokumentControllerV2

    @MockBean
    lateinit var pdfService: PdfService

    @MockBean
    lateinit var metrikk: Metrikk

    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    @Before
    fun setup() {
        loggInnBrukerTokenX(contextHolder, ARBEIDSTAKER_FNR, oppfolgingsplanClientId, tokenxIdp)
    }

    @Test
    fun hent_pdf_som_bruker() {
        val oppfolgingsplanId = 1L
        val pdf = ByteArray(10)
        `when`(pdfService.hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(pdf)
        val response = dokumentController.hentPdf(oppfolgingsplanId)
        verify(pdfService).hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        assertEquals(200, response.statusCodeValue.toLong())
        assertEquals(pdf, response.body)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_hent_pdf() {
        loggUtAlle(contextHolder)
        dokumentController.hentPdf(1)
    }
}
