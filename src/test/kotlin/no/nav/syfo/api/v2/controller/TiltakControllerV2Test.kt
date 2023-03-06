package no.nav.syfo.api.v2.controller


import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v2.domain.oppfolgingsplan.KommentarRequest
import no.nav.syfo.domain.Kommentar
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.KommentarService
import no.nav.syfo.service.TiltakService
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import org.junit.Assert.assertEquals
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
        val tiltakId = 1L
        tiltakController.slettTiltak(tiltakId)
        verify(tiltakService).slettTiltak(tiltakId, ARBEIDSTAKER_FNR)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_slett_tiltak() {
        loggUtAlle(contextHolder)
        tiltakController.slettTiltak(1)
    }

    @Test
    fun lagrer_kommentar_som_bruker() {
        val tiltakId = 1L
        val kommentarTekst = "Kommentar"
        val kommentarRequest = KommentarRequest(tekst = kommentarTekst)
        val kommentar = Kommentar().tekst(kommentarTekst)
        val kommentarId = 1L
        `when`(kommentarService.lagreKommentar(tiltakId, kommentar, ARBEIDSTAKER_FNR)).thenReturn(kommentarId)
        val res = tiltakController.lagreKommentar(tiltakId, kommentarRequest)
        verify(kommentarService).lagreKommentar(eq(tiltakId), any(no.nav.syfo.domain.Kommentar::class.java), eq(ARBEIDSTAKER_FNR))
        verify(metrikk).tellHendelse("lagre_kommentar")
        assertEquals(kommentarId, res)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_lagre_kommentar() {
        val tiltakId = 1L
        val kommentarRequest =  KommentarRequest(1L, "")
        loggUtAlle(contextHolder)
        tiltakController.lagreKommentar(tiltakId, kommentarRequest)
    }
}
