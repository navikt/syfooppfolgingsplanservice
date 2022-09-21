package no.nav.syfo.api.v2.controller


import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.selvbetjening.controller.TiltakController
import no.nav.syfo.api.selvbetjening.domain.RSKommentar
import no.nav.syfo.domain.Kommentar
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.KommentarService
import no.nav.syfo.service.TiltakService
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import no.nav.syfo.util.MapUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class TiltakControllerV2Test : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var tiltakController: TiltakControllerV2

    @MockBean
    lateinit var kommentarService: KommentarService

    @MockBean
    lateinit var tiltakService: TiltakService

    @MockBean
    lateinit var metrikk: Metrikk

    private val tiltakId = 1L

    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    @Before
    fun setup() {
        loggInnBrukerTokenX(contextHolder, ARBEIDSTAKER_FNR, oppfolgingsplanClientId, tokenxIdp)
    }

    @Test
    fun sletter_tiltak_som_bruker() {
        tiltakController.slettTiltak(tiltakId)
        verify(tiltakService).slettTiltak(tiltakId, ARBEIDSTAKER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_slett_tiltak() {
        loggUtAlle(contextHolder)
        tiltakController.slettTiltak(tiltakId)
    }

    @Test
    fun lagrer_kommentar_som_bruker() {
        val rsKommentar = RSKommentar()
            .tekst("Kommentar")
        val kommentar = MapUtil.map(rsKommentar, TiltakController.rsKommentar2kommentar)
        val kommentarId = 1L
        `when`(kommentarService.lagreKommentar(tiltakId, kommentar, ARBEIDSTAKER_FNR)).thenReturn(kommentarId)
        val res = tiltakController.lagreKommentar(tiltakId, rsKommentar)
        verify(kommentarService).lagreKommentar(eq<Long>(tiltakId), any(Kommentar::class.java), eq(ARBEIDSTAKER_FNR))
        verify(metrikk).tellHendelse("lagre_kommentar")
        Assert.assertEquals(res, kommentarId)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_lagre_kommentar() {
        loggUtAlle(contextHolder)
        val kommentar = RSKommentar()
        tiltakController.lagreKommentar(tiltakId, kommentar)
    }
}
