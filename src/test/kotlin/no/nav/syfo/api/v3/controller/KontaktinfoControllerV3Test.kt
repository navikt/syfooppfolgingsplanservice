package no.nav.syfo.api.v3.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v3.domain.Kontaktinfo
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.dkif.DigitalKontaktinfo
import no.nav.syfo.dkif.DkifConsumer
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.PERSON_EMAIL
import no.nav.syfo.testhelper.UserConstants.PERSON_TLF
import no.nav.syfo.testhelper.generateDigitalKontaktinfo
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import javax.inject.Inject

class KontaktinfoControllerV3Test : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var dkifConsumer: DkifConsumer

    @Inject
    private lateinit var kontaktinfoController: KontaktinfoControllerV3

    @Test
    fun narmesteLeder_ansatt_ok() {
        tokenValidationTestUtil.logInAsUser(LEDER_FNR)

        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        val digitalKontaktinfo = DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = false,
            reservert = false,
            mobiltelefonnummer = PERSON_TLF
        )
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCode.value().toLong())
        assertEquals(ARBEIDSTAKER_FNR, body.fnr)
        assertEquals(digitalKontaktinfo.epostadresse, body.epost)
        assertEquals(digitalKontaktinfo.mobiltelefonnummer, body.tlf)
        assertNull(body.feilAarsak)
    }

    @Test
    fun narmesteLeder_self_ok() {
        tokenValidationTestUtil.logInAsUser(ARBEIDSTAKER_FNR)
        val digitalKontaktinfo = generateDigitalKontaktinfo()
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCode.value().toLong())
        assertEquals(ARBEIDSTAKER_FNR, body.fnr)
        assertEquals(digitalKontaktinfo.epostadresse, body.epost)
        assertEquals(digitalKontaktinfo.mobiltelefonnummer, body.tlf)
        assertNull(body.feilAarsak)
    }

    @Test
    fun narmesteLeder_forbidden() {
        tokenValidationTestUtil.logInAsUser(LEDER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(false)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        assertEquals(403, res.statusCode.value().toLong())
    }

    @Test
    fun skal_ha_varsel_naar_kan_varsles_og_er_ikke_reservert() {
        tokenValidationTestUtil.logInAsUser(LEDER_FNR)

        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        val digitalKontaktinfo = DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = true,
            reservert = false,
            mobiltelefonnummer = PERSON_TLF
        )
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCode.value().toLong())
        assertTrue(body.skalHaVarsel)
    }

    @Test
    fun skal_ikke_ha_varsel_naar_ikke_kan_varsles_og_er_ikke_reservert() {
        tokenValidationTestUtil.logInAsUser(LEDER_FNR)

        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        val digitalKontaktinfo = DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = false,
            reservert = false,
            mobiltelefonnummer = PERSON_TLF
        )
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCode.value().toLong())
        assertFalse(body.skalHaVarsel)
    }

    @Test
    fun skal_ikke_ha_varsel_naar_ikke_kan_varsles_og_er_reservert() {
        tokenValidationTestUtil.logInAsUser(LEDER_FNR)

        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        val digitalKontaktinfo = DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = false,
            reservert = true,
            mobiltelefonnummer = PERSON_TLF
        )
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCode.value().toLong())
        assertFalse(body.skalHaVarsel)
    }

    @Test
    fun skal_ikke_ha_varsel_naar_kan_varsles_og_er_reservert() {
        tokenValidationTestUtil.logInAsUser(LEDER_FNR)

        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        val digitalKontaktinfo = DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = true,
            reservert = true,
            mobiltelefonnummer = PERSON_TLF
        )
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCode.value().toLong())
        assertFalse(body.skalHaVarsel)
    }

}
