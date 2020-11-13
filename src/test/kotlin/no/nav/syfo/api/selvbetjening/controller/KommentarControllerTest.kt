package no.nav.syfo.api.selvbetjening.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.KommentarService
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class KommentarControllerTest : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var kommentarController: KommentarController

    @MockBean
    lateinit var kommentarService: KommentarService

    @MockBean
    lateinit var metrikk: Metrikk

    @Before
    fun setup() {
        loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
    }

    @Test
    fun sletter_tiltak_som_bruker() {
        kommentarController.slettKommentar(kommentarId)
        Mockito.verify(kommentarService).slettKommentar(kommentarId, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("slett_kommentar")
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_slett_tiltak() {
        loggUtAlle(contextHolder)
        kommentarController.slettKommentar(kommentarId)
    }

    companion object {
        private const val kommentarId = 1L
    }
}
