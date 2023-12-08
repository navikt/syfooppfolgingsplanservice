package no.nav.syfo.api.intern.controller.v2

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.intern.v2.DokumentADControllerV2
import no.nav.syfo.domain.GodkjentPlan
import no.nav.syfo.repository.dao.GodkjentplanDAO
import no.nav.syfo.service.DokumentService
import no.nav.syfo.service.PdfService
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.testhelper.mockSvarFraIstilgangskontrollTilgangTilSYFO
import org.junit.*
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import java.io.IOException
import java.text.ParseException
import java.util.*
import javax.inject.Inject
import jakarta.ws.rs.ForbiddenException

class DokumentControllerV2Test : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var dokumentController: DokumentADControllerV2

    @MockBean
    private lateinit var dokumentService: DokumentService

    @MockBean
    private lateinit var godkjentplanDAO: GodkjentplanDAO

    @MockBean
    private lateinit var pdfService: PdfService

    @Before
    @Throws(ParseException::class)
    fun setup() {
        tokenValidationTestUtil.logInAsNavCounselor(VEILEDER_ID)
    }

    @After
    override fun tearDown() {
        tokenValidationTestUtil.logout()
    }

    @Test
    @Throws(IOException::class)
    fun bilde_har_tilgang() {
        mockSvarFraIstilgangskontrollSyfo(HttpStatus.OK)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(1)).thenReturn(Optional.of(GodkjentPlan().dokumentUuid("1")))
        Mockito.`when`(dokumentService.hentDokument("1")).thenReturn(byteArrayOf())
        dokumentController.bilde(1L, 1)
    }

    @Test(expected = ForbiddenException::class)
    @Throws(IOException::class)
    fun bilde_har_ikke_tilgang() {
        mockSvarFraIstilgangskontrollSyfo(HttpStatus.FORBIDDEN)
        dokumentController.bilde(1L, 1)
    }

    @Test
    fun dokumentinfo_har_tilgang() {
        mockSvarFraIstilgangskontrollSyfo(HttpStatus.OK)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(1)).thenReturn(Optional.of(GodkjentPlan().dokumentUuid("1")))
        Mockito.`when`(dokumentService.hentDokument("1")).thenReturn(byteArrayOf())
        dokumentController.dokumentinfo(1L)
    }

    @Test(expected = ForbiddenException::class)
    fun dokumentinfo_har_ikke_tilgang() {
        mockSvarFraIstilgangskontrollSyfo(HttpStatus.FORBIDDEN)
        dokumentController.dokumentinfo(1L)
    }

    @Test
    fun dokument_har_tilgang() {
        mockSvarFraIstilgangskontrollSyfo(HttpStatus.OK)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(1)).thenReturn(Optional.of(GodkjentPlan().dokumentUuid("1")))
        Mockito.`when`(dokumentService.hentDokument("1")).thenReturn(byteArrayOf())
        dokumentController.dokument(1L)
    }

    @Test(expected = ForbiddenException::class)
    fun dokument_har_ikke_tilgang() {
        mockSvarFraIstilgangskontrollSyfo(HttpStatus.FORBIDDEN)
        dokumentController.dokument(1L)
    }

    @Test(expected = RuntimeException::class)
    @Throws(IOException::class)
    fun bilde_annen_tilgangsfeil() {
        mockSvarFraIstilgangskontrollSyfo(HttpStatus.INTERNAL_SERVER_ERROR)
        dokumentController.bilde(1L, 1)
    }

    @Test(expected = RuntimeException::class)
    fun dokument_annen_tilgangsfeil() {
        mockSvarFraIstilgangskontrollSyfo(HttpStatus.INTERNAL_SERVER_ERROR)
        dokumentController.dokument(1L)
    }

    private fun mockSvarFraIstilgangskontrollSyfo(
        status: HttpStatus,
    ) {
        mockSvarFraIstilgangskontrollTilgangTilSYFO(
            azureTokenEndpoint = azureTokenEndpoint,
            tilgangskontrollUrl = tilgangskontrollUrl,
            mockRestServiceServer = mockRestServiceServer,
            mockRestServiceWithProxyServer = mockRestServiceWithProxyServer,
            status = status,
        )
    }
}
