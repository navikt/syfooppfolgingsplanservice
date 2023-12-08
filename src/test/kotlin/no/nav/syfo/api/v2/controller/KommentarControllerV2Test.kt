package no.nav.syfo.api.v2.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.KommentarService
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import org.junit.Test
import org.mockito.Mockito.verify
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class KommentarControllerV2Test : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var kommentarController: KommentarControllerV2

    @MockBean
    lateinit var kommentarService: KommentarService

    @MockBean
    lateinit var metrikk: Metrikk

    @Test
    fun sletter_tiltak_som_bruker() {
        tokenValidationTestUtil.logInAsUser(ARBEIDSTAKER_FNR)
        val kommentarId = 1L
        kommentarController.slettKommentar(kommentarId)
        verify(kommentarService).slettKommentar(kommentarId, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("slett_kommentar")
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_slett_tiltak() {
        kommentarController.slettKommentar(1)
    }
}
