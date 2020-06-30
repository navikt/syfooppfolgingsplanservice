package no.nav.syfo.api.intern.controller

import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.intern.controller.OppfolgingsplanInternController
import no.nav.syfo.ereg.EregConsumer
import no.nav.syfo.repository.dao.OppfolgingsplanDAO
import no.nav.syfo.service.BrukerprofilService
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.VEILEDER_ID
import no.nav.syfo.testhelper.loggInnVeilederAzure
import org.junit.*
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import java.text.ParseException
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

class OppfolgingsplanInternControllerTest : AbstractRessursTilgangTest() {
    @MockBean
    private lateinit var aktorregisterConsumer: AktorregisterConsumer

    @MockBean
    private lateinit var eregConsumer: EregConsumer

    @MockBean
    private lateinit var oppfolgingsplanDAO: OppfolgingsplanDAO

    @MockBean
    private lateinit var brukerprofilService: BrukerprofilService

    @Inject
    private lateinit var oppfolgingsplanInternController: OppfolgingsplanInternController

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
    fun historyHasAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK)
        oppfolgingsplanInternController.getHistorikk(ARBEIDSTAKER_FNR)
    }

    @Test(expected = ForbiddenException::class)
    fun historyNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        oppfolgingsplanInternController.getHistorikk(ARBEIDSTAKER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun historyServerError() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR)
        oppfolgingsplanInternController.getHistorikk(ARBEIDSTAKER_FNR)
    }

    @Test
    fun oppfolgingsplanerHasAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.OK)
        oppfolgingsplanInternController.getOppfolgingsplaner(ARBEIDSTAKER_FNR)
    }

    @Test(expected = ForbiddenException::class)
    fun oppfolgingsplanerNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.FORBIDDEN)
        oppfolgingsplanInternController.getOppfolgingsplaner(ARBEIDSTAKER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun oppfolgingsplanerServerError() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, HttpStatus.INTERNAL_SERVER_ERROR)
        oppfolgingsplanInternController.getOppfolgingsplaner(ARBEIDSTAKER_FNR)
    }
}
