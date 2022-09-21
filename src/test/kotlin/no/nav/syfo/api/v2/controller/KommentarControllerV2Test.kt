package no.nav.syfo.api.v2.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.KommentarService
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import org.junit.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class KommentarControllerV2Test : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var kommentarController: KommentarControllerV2

    @MockBean
    lateinit var kommentarService: KommentarService

    @MockBean
    lateinit var metrikk: Metrikk

    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    @Test
    fun sletter_tiltak_som_bruker() {
        loggInnBrukerTokenX(contextHolder, ARBEIDSTAKER_FNR, oppfolgingsplanClientId, tokenxIdp)
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
