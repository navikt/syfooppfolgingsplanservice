package no.nav.syfo.api.intern.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.intern.DokumentADController
import no.nav.syfo.domain.GodkjentPlan
import no.nav.syfo.repository.dao.GodkjentplanDAO
import no.nav.syfo.service.DokumentService
import no.nav.syfo.service.PdfService
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.testhelper.loggInnVeilederAzure
import org.junit.*
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import java.io.IOException
import java.text.ParseException
import java.util.*
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

class DokumentControllerTest : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var dokumentController: DokumentADController

    @MockBean
    private lateinit var dokumentService: DokumentService

    @MockBean
    private lateinit var godkjentplanDAO: GodkjentplanDAO

    @MockBean
    private lateinit var pdfService: PdfService

    @Before
    @Throws(ParseException::class)
    fun setup() {
        loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID)
    }

    @After
    override fun tearDown() {
        loggUtAlle(oidcRequestContextHolder)
    }

    @Test
    @Throws(IOException::class)
    fun bilde_har_tilgang() {
        mockSvarFraTilgangTilTjenestenViaAzure(HttpStatus.OK)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(1)).thenReturn(Optional.of(GodkjentPlan().dokumentUuid("1")))
        Mockito.`when`(dokumentService.hentDokument("1")).thenReturn(byteArrayOf())
        dokumentController.bilde(1L, 1)
    }

    @Test(expected = ForbiddenException::class)
    @Throws(IOException::class)
    fun bilde_har_ikke_tilgang() {
        mockSvarFraTilgangTilTjenestenViaAzure(HttpStatus.FORBIDDEN)
        dokumentController.bilde(1L, 1)
    }

    @Test
    fun dokumentinfo_har_tilgang() {
        mockSvarFraTilgangTilTjenestenViaAzure(HttpStatus.OK)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(1)).thenReturn(Optional.of(GodkjentPlan().dokumentUuid("1")))
        Mockito.`when`(dokumentService.hentDokument("1")).thenReturn(byteArrayOf())
        dokumentController.dokumentinfo(1L)
    }

    @Test(expected = ForbiddenException::class)
    fun dokumentinfo_har_ikke_tilgang() {
        mockSvarFraTilgangTilTjenestenViaAzure(HttpStatus.FORBIDDEN)
        dokumentController.dokumentinfo(1L)
    }

    @Test
    fun dokument_har_tilgang() {
        mockSvarFraTilgangTilTjenestenViaAzure(HttpStatus.OK)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(1)).thenReturn(Optional.of(GodkjentPlan().dokumentUuid("1")))
        Mockito.`when`(dokumentService.hentDokument("1")).thenReturn(byteArrayOf())
        dokumentController.dokument(1L)
    }

    @Test(expected = ForbiddenException::class)
    fun dokument_har_ikke_tilgang() {
        mockSvarFraTilgangTilTjenestenViaAzure(HttpStatus.FORBIDDEN)
        dokumentController.dokument(1L)
    }

    @Test(expected = RuntimeException::class)
    @Throws(IOException::class)
    fun bilde_annen_tilgangsfeil() {
        mockSvarFraTilgangTilTjenestenViaAzure(HttpStatus.INTERNAL_SERVER_ERROR)
        dokumentController.bilde(1L, 1)
    }

    @Test(expected = RuntimeException::class)
    fun dokument_annen_tilgangsfeil() {
        mockSvarFraTilgangTilTjenestenViaAzure(HttpStatus.INTERNAL_SERVER_ERROR)
        dokumentController.dokument(1L)
    }
}
