package no.nav.syfo.api.intern.controller.v2

import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.ereg.EregConsumer
import no.nav.syfo.repository.dao.OppfolgingsplanDAO
import no.nav.syfo.service.BrukerprofilService
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.testhelper.loggInnVeilederAzureADV2
import no.nav.syfo.testhelper.mockSvarFraSyfoTilgangskontrollV2TilgangTilBruker
import org.junit.*
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import java.text.ParseException
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

class OppfolgingsplanInternControllerV2Test : AbstractRessursTilgangTest() {
    @MockBean
    private lateinit var aktorregisterConsumer: AktorregisterConsumer

    @MockBean
    private lateinit var eregConsumer: EregConsumer

    @MockBean
    private lateinit var oppfolgingsplanDAO: OppfolgingsplanDAO

    @MockBean
    private lateinit var brukerprofilService: BrukerprofilService

    @Inject
    private lateinit var oppfolgingsplanInternController: OppfolgingsplanInternControllerV2

    @Before
    @Throws(ParseException::class)
    fun setup() {
        loggInnVeilederAzureADV2(contextHolder, VEILEDER_ID)
    }

    @After
    override fun tearDown() {
        loggUtAlle(contextHolder)
    }

    @Test
    fun historyHasAccess() {
        mockSvarFraSyfoTilgangskontrollBruker(ARBEIDSTAKER_FNR, HttpStatus.OK)
        oppfolgingsplanInternController.getHistorikk(ARBEIDSTAKER_FNR)
    }

    @Test(expected = ForbiddenException::class)
    fun historyNoAccess() {
        mockSvarFraSyfoTilgangskontrollBruker(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        oppfolgingsplanInternController.getHistorikk(ARBEIDSTAKER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun historyServerError() {
        mockSvarFraSyfoTilgangskontrollBruker(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR)
        oppfolgingsplanInternController.getHistorikk(ARBEIDSTAKER_FNR)
    }

    @Test
    fun oppfolgingsplanerHasAccess() {
        mockSvarFraSyfoTilgangskontrollBruker(ARBEIDSTAKER_FNR, HttpStatus.OK)
        oppfolgingsplanInternController.getOppfolgingsplaner(ARBEIDSTAKER_FNR)
    }

    @Test(expected = ForbiddenException::class)
    fun oppfolgingsplanerNoAccess() {
        mockSvarFraSyfoTilgangskontrollBruker(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        oppfolgingsplanInternController.getOppfolgingsplaner(ARBEIDSTAKER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun oppfolgingsplanerServerError() {
        mockSvarFraSyfoTilgangskontrollBruker(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR)
        oppfolgingsplanInternController.getOppfolgingsplaner(ARBEIDSTAKER_FNR)
    }

    private fun mockSvarFraSyfoTilgangskontrollBruker(
        fnr: String,
        status: HttpStatus
    ) {
        mockSvarFraSyfoTilgangskontrollV2TilgangTilBruker(
            azureTokenEndpoint = azureTokenEndpoint,
            tilgangskontrollUrl = tilgangskontrollUrl,
            mockRestServiceServer = mockRestServiceServer,
            mockRestServiceWithProxyServer = mockRestServiceWithProxyServer,
            status = status,
            fnr = fnr
        )
    }
}
