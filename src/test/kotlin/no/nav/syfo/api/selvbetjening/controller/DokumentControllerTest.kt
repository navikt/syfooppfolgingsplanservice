package no.nav.syfo.api.selvbetjening.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.PdfService
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import org.junit.*
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import java.io.IOException
import javax.inject.Inject

class DokumentControllerTest : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var dokumentController: DokumentController

    @MockBean
    lateinit var pdfService: PdfService

    @MockBean
    lateinit var metrikk: Metrikk

    @Before
    fun setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR)
    }

    @Test
    fun hent_pdf_som_bruker() {
        val pdf = ByteArray(10)
        Mockito.`when`(pdfService.hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(pdf)
        val response = dokumentController.hentPdf(oppfolgingsplanId)
        Mockito.verify(pdfService).hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        Assert.assertEquals(200, response.statusCodeValue.toLong())
        Assert.assertEquals(pdf, response.body)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_hent_pdf() {
        loggUtAlle(oidcRequestContextHolder)
        dokumentController.hentPdf(oppfolgingsplanId)
    }

    @Test
    fun hentSidebilde_som_bruker() {
        val pdf = ByteArray(10)
        val sideantall = 3
        Mockito.`when`(pdfService.hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(pdf)
        Mockito.`when`(pdfService.hentAntallSiderIDokument(pdf)).thenReturn(sideantall)
        var response: ResponseEntity<*> = ResponseEntity.badRequest().build<Any>()
        try {
            Mockito.`when`(pdfService.pdf2image(pdf, sideId.toInt())).thenReturn(pdf)
            response = dokumentController.hentSidebilde(oppfolgingsplanId, sideId)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Mockito.verify(metrikk).tellHendelse("hent_sidebilde")
        Assert.assertEquals(200, response.statusCodeValue.toLong())
        Assert.assertEquals(pdf, response.body)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_hentSidebilde() {
        loggUtAlle(oidcRequestContextHolder)
        try {
            dokumentController.hentSidebilde(oppfolgingsplanId, sideId)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Test
    fun hentPdfurler_som_bruker() {
        val pdf = ByteArray(10)
        val sideantall = 3
        Mockito.`when`(pdfService.hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(pdf)
        Mockito.`when`(pdfService.hentAntallSiderIDokument(pdf)).thenReturn(sideantall)
        val returnertListe = dokumentController.hentPdfurler(oppfolgingsplanId)
        val forventetUrl = "https://syfoapi.nav.no/syfooppfolgingsplanservice/api/dokument/$oppfolgingsplanId/side/"
        Mockito.verify(metrikk).tellHendelse("hent_pdfurler")
        Assert.assertEquals(sideantall.toLong(), returnertListe.size.toLong())
        for (i in 0 until sideantall) {
            Assert.assertEquals(forventetUrl + (i + 1), returnertListe[i])
        }
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_hentPdfurler() {
        loggUtAlle(oidcRequestContextHolder)
        dokumentController.hentPdfurler(oppfolgingsplanId)
    }

    @Test
    fun hentSyfoapiUrl_default() {
        val returnertVerdi = dokumentController.hentSyfoapiUrl("")
        val forventetVerdi = "https://syfoapi.nav.no"
        Assert.assertEquals(forventetVerdi, returnertVerdi)
    }

    @Test
    fun hentSyfoapiUrl_prod() {
        val returnertVerdi = dokumentController.hentSyfoapiUrl("prod-fss")
        val forventetVerdi = "https://syfoapi.nav.no"
        Assert.assertEquals(forventetVerdi, returnertVerdi)
    }

    @Test
    fun hentSyfoapiUrl_preprod() {
        val returnertVerdi = dokumentController.hentSyfoapiUrl("dev-fss")
        val forventetVerdi = "https://syfoapi-q.nav.no"
        Assert.assertEquals(forventetVerdi, returnertVerdi)
    }

    companion object {
        private const val oppfolgingsplanId = 1L
        private const val sideId = 1L
    }
}
