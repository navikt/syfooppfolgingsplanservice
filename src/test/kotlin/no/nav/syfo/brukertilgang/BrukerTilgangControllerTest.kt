package no.nav.syfo.brukertilgang

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.service.BrukertilgangService
import no.nav.syfo.testhelper.OidcTestHelper
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER_DEPRECATED
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

class BrukerTilgangControllerTest : AbstractRessursTilgangTest() {

    @MockBean
    private lateinit var brukertilgangConsumer: BrukertilgangConsumer
    @MockBean
    private lateinit var brukertilgangService: BrukertilgangService
    @MockBean
    private lateinit var pdlConsumer: PdlConsumer

    @Inject
    private lateinit var brukerTilgangController: BrukerTilgangController

    @Test
    fun sjekk_tilgang_til_bruker() {
        loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        `when`(pdlConsumer.isKode6Or7(UserConstants.ARBEIDSTAKER_FNR)).thenReturn(false)
        `when`(brukertilgangService.tilgangTilOppslattIdent(UserConstants.LEDER_FNR, UserConstants.ARBEIDSTAKER_FNR)).thenReturn(true)

        val exp = RSTilgang(
                true,
                null
        )
        val res = brukerTilgangController.harTilgang(UserConstants.ARBEIDSTAKER_FNR)
        assertEquals(exp, res)
    }

    @Test
    fun sjekk_tilgang_til_seg_selv() {
        loggInnBruker(contextHolder, UserConstants.ARBEIDSTAKER_FNR)
        `when`(pdlConsumer.isKode6Or7(UserConstants.ARBEIDSTAKER_FNR)).thenReturn(false)
        `when`(brukertilgangService.tilgangTilOppslattIdent(UserConstants.ARBEIDSTAKER_FNR, UserConstants.ARBEIDSTAKER_FNR)).thenReturn(true)

        val exp = RSTilgang(true, null)
        val res = brukerTilgangController.harTilgang(null)
        assertEquals(exp, res)
    }

    @Test
    fun sjekk_tilgang_til_bruker_diskresjonsmerket() {
        loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        `when`(pdlConsumer.isKode6Or7(UserConstants.ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(brukertilgangService.tilgangTilOppslattIdent(UserConstants.LEDER_FNR, UserConstants.ARBEIDSTAKER_FNR)).thenReturn(true)

        val exp = RSTilgang(false, BrukerTilgangController.IKKE_TILGANG_GRUNN_DISKRESJONSMERKET)
        val res = brukerTilgangController.harTilgang(UserConstants.ARBEIDSTAKER_FNR)
        assertEquals(exp, res)
    }

    @Test
    fun sjekk_tilgang_til_seg_selv_diskresjonsmerket() {
        loggInnBruker(contextHolder, UserConstants.ARBEIDSTAKER_FNR)
        `when`(pdlConsumer.isKode6Or7(UserConstants.ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(brukertilgangService.tilgangTilOppslattIdent(UserConstants.ARBEIDSTAKER_FNR, UserConstants.ARBEIDSTAKER_FNR)).thenReturn(true)

        val exp = RSTilgang(false, BrukerTilgangController.IKKE_TILGANG_GRUNN_DISKRESJONSMERKET)
        val res = brukerTilgangController.harTilgang(null)
        assertEquals(exp, res)
    }

    @Test(expected = ForbiddenException::class)
    fun sjekk_tilgang_til_bruker_ikke_tilgang() {
        loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        `when`(brukertilgangService.tilgangTilOppslattIdent(UserConstants.LEDER_FNR, UserConstants.ARBEIDSTAKER_FNR)).thenReturn(false)

        brukerTilgangController.harTilgang(UserConstants.ARBEIDSTAKER_FNR)
    }

    @Test(expected = ForbiddenException::class)
    fun sjekk_tilgang_til_seg_selv_ikke_tilgang() {
        loggInnBruker(contextHolder, UserConstants.ARBEIDSTAKER_FNR)
        `when`(brukertilgangService.tilgangTilOppslattIdent(UserConstants.ARBEIDSTAKER_FNR, UserConstants.ARBEIDSTAKER_FNR)).thenReturn(false)

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
    fun accessToIdent_granted() {
        loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER, UserConstants.ARBEIDSTAKER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(UserConstants.ARBEIDSTAKER_FNR)).thenReturn(false)

        val (tilgang) = brukerTilgangController.accessToAnsatt(headers)
        assertFalse(tilgang)
    }

    @Test
    fun accessToIdent_granted_v1() {
        loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER_DEPRECATED, UserConstants.ARBEIDSTAKER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(UserConstants.ARBEIDSTAKER_FNR)).thenReturn(false)

        val (tilgang) = brukerTilgangController.accessToAnsatt(headers)
        assertFalse(tilgang)
    }

    @Test
    fun accessToIdent_denied() {
        loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add(NAV_PERSONIDENT_HEADER_DEPRECATED, UserConstants.ARBEIDSTAKER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(UserConstants.ARBEIDSTAKER_FNR)).thenReturn(true)

        val (tilgang) = brukerTilgangController.accessToAnsatt(headers)
        assertTrue(tilgang)
    }
}
