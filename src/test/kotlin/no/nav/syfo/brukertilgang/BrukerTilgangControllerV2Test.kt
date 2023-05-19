package no.nav.syfo.brukertilgang

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.brukertilgang.v2.BrukerTilgangControllerV2
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.service.BrukertilgangService
import no.nav.syfo.testhelper.OidcTestHelper
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

class BrukerTilgangControllerV2Test : AbstractRessursTilgangTest() {

    @MockBean
    private lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    private lateinit var brukertilgangService: BrukertilgangService

    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @Inject
    private lateinit var brukerTilgangController: BrukerTilgangControllerV2

    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    @Before
    fun setup() {
    }

    @Test
    fun sjekk_tilgang_til_bruker() {
        loggInnBruker(LEDER_FNR)
        `when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        `when`(brukertilgangService.tilgangTilOppslattIdent(LEDER_FNR, ARBEIDSTAKER_FNR)).thenReturn(true)

        val exp = RSTilgang(
            true,
            null,
        )
        val res = brukerTilgangController.harTilgang(ARBEIDSTAKER_FNR)
        assertEquals(exp, res)
    }

    @Test
    fun sjekk_tilgang_til_seg_selv() {
        loggInnBruker(ARBEIDSTAKER_FNR)
        `when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(false)
        `when`(brukertilgangService.tilgangTilOppslattIdent(ARBEIDSTAKER_FNR, ARBEIDSTAKER_FNR)).thenReturn(true)

        val exp = RSTilgang(true, null)
        val res = brukerTilgangController.harTilgang(null)
        assertEquals(exp, res)
    }

    @Test
    fun sjekk_tilgang_til_bruker_diskresjonsmerket() {
        loggInnBruker(LEDER_FNR)
        `when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(brukertilgangService.tilgangTilOppslattIdent(LEDER_FNR, ARBEIDSTAKER_FNR)).thenReturn(true)

        val exp = RSTilgang(true)
        val res = brukerTilgangController.harTilgang(ARBEIDSTAKER_FNR)
        assertEquals(exp, res)
    }

    @Test
    fun sjekk_tilgang_til_seg_selv_diskresjonsmerket() {
        loggInnBruker(ARBEIDSTAKER_FNR)
        `when`(pdlConsumer.isKode6Or7(ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(brukertilgangService.tilgangTilOppslattIdent(ARBEIDSTAKER_FNR, ARBEIDSTAKER_FNR)).thenReturn(true)

        val exp = RSTilgang(true)
        val res = brukerTilgangController.harTilgang(null)
        assertEquals(exp, res)
    }

    @Test(expected = ForbiddenException::class)
    fun sjekk_tilgang_til_bruker_ikke_tilgang() {
        loggInnBruker(LEDER_FNR)
        `when`(brukertilgangService.tilgangTilOppslattIdent(LEDER_FNR, ARBEIDSTAKER_FNR)).thenReturn(false)

        brukerTilgangController.harTilgang(ARBEIDSTAKER_FNR)
    }

    @Test(expected = ForbiddenException::class)
    fun sjekk_tilgang_til_seg_selv_ikke_tilgang() {
        loggInnBruker(ARBEIDSTAKER_FNR)
        `when`(brukertilgangService.tilgangTilOppslattIdent(ARBEIDSTAKER_FNR, ARBEIDSTAKER_FNR)).thenReturn(false)

        brukerTilgangController.harTilgang(null)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker() {
        OidcTestHelper.loggUtAlle(contextHolder)
        brukerTilgangController.harTilgang(null)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_ved_oppslag() {
        OidcTestHelper.loggUtAlle(contextHolder)
        brukerTilgangController.harTilgang("")
    }

    @Test
    fun accessToIdent_denied() {
        loggInnBruker(LEDER_FNR)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(false)

        val (tilgang) = brukerTilgangController.accessToAnsatt(headers)
        assertFalse(tilgang)
    }

    @Test
    fun accessToIdent_granted() {
        loggInnBruker(LEDER_FNR)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)

        val (tilgang) = brukerTilgangController.accessToAnsatt(headers)
        assertTrue(tilgang)
    }

    fun loggInnBruker(fnr: String) {
        loggInnBrukerTokenX(contextHolder, fnr, oppfolgingsplanClientId, tokenxIdp)
    }
}
