package no.nav.syfo.api.selvbetjening.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.selvbetjening.domain.RSKommentar
import no.nav.syfo.domain.Kommentar
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.KommentarService
import no.nav.syfo.service.TiltakService
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.util.MapUtil
import org.junit.*
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class TiltakControllerTest : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var tiltakController: TiltakController

    @MockBean
    lateinit var kommentarService: KommentarService

    @MockBean
    lateinit var tiltakService: TiltakService

    @MockBean
    lateinit var metrikk: Metrikk

    @Before
    fun setup() {
        loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
    }

    @Test
    fun sletter_tiltak_som_bruker() {
        tiltakController.slettTiltak(tiltakId)
        Mockito.verify(tiltakService).slettTiltak(tiltakId, ARBEIDSTAKER_FNR)
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
        Mockito.`when`(kommentarService.lagreKommentar(tiltakId, kommentar, ARBEIDSTAKER_FNR)).thenReturn(kommentarId)
        val res = tiltakController.lagreKommentar(tiltakId, rsKommentar)
        Mockito.verify(kommentarService).lagreKommentar(ArgumentMatchers.eq<Long>(tiltakId), ArgumentMatchers.any(Kommentar::class.java), ArgumentMatchers.eq<String>(ARBEIDSTAKER_FNR))
        Mockito.verify(metrikk).tellHendelse("lagre_kommentar")
        Assert.assertEquals(res, kommentarId)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_lagre_kommentar() {
        loggUtAlle(contextHolder)
        val kommentar = RSKommentar()
        tiltakController.lagreKommentar(tiltakId, kommentar)
    }

    companion object {
        private const val tiltakId = 1L
    }
}
