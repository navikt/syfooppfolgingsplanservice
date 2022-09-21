package no.nav.syfo.api.v3.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v2.domain.Kontaktinfo
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.dkif.DigitalKontaktinfo
import no.nav.syfo.dkif.DkifConsumer
import no.nav.syfo.sts.StsConsumer
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.PERSON_EMAIL
import no.nav.syfo.testhelper.UserConstants.PERSON_TLF
import no.nav.syfo.testhelper.generateDigitalKontaktinfo
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import javax.inject.Inject

class KontaktinfoControllerV3Test : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var dkifConsumer: DkifConsumer

    @MockBean
    private lateinit var stsConsumer: StsConsumer

    @Inject
    private lateinit var kontaktinfoController: KontaktinfoControllerV3

    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    @Before
    fun setup() {
        `when`(stsConsumer.token()).thenReturn("token")

    }

    @Test
    fun narmesteLeder_ansatt_ok() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)

        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        val digitalKontaktinfo = DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = false,
            reservert = false,
            mobiltelefonnummer = PERSON_TLF,
            personident = ARBEIDSTAKER_FNR
        )
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(ARBEIDSTAKER_FNR, body.fnr)
        assertEquals(digitalKontaktinfo.epostadresse, body.epost)
        assertEquals(digitalKontaktinfo.mobiltelefonnummer, body.tlf)
        assertNull(body.feilAarsak)
    }

    @Test
    fun narmesteLeder_self_ok() {
        loggInnBrukerTokenX(contextHolder, ARBEIDSTAKER_FNR, oppfolgingsplanClientId, tokenxIdp)
        val digitalKontaktinfo = generateDigitalKontaktinfo()
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(ARBEIDSTAKER_FNR, body.fnr)
        assertEquals(digitalKontaktinfo.epostadresse, body.epost)
        assertEquals(digitalKontaktinfo.mobiltelefonnummer, body.tlf)
        assertNull(body.feilAarsak)
    }

    @Test
    fun narmesteLeder_forbidden() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(false)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        assertEquals(403, res.statusCodeValue.toLong())
    }

    @Test
    fun skal_ha_varsel_naar_kan_varsles_og_er_ikke_reservert() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)

        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        val digitalKontaktinfo = DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = true,
            reservert = false,
            mobiltelefonnummer = PERSON_TLF,
            personident = ARBEIDSTAKER_FNR
        )
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCodeValue.toLong())
        assertTrue(body.skalHaVarsel)
    }

    @Test
    fun skal_ikke_ha_varsel_naar_ikke_kan_varsles_og_er_ikke_reservert() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)

        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        val digitalKontaktinfo = DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = false,
            reservert = false,
            mobiltelefonnummer = PERSON_TLF,
            personident = ARBEIDSTAKER_FNR
        )
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCodeValue.toLong())
        assertFalse(body.skalHaVarsel)
    }

    @Test
    fun skal_ikke_ha_varsel_naar_ikke_kan_varsles_og_er_reservert() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)

        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        val digitalKontaktinfo = DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = false,
            reservert = true,
            mobiltelefonnummer = PERSON_TLF,
            personident = ARBEIDSTAKER_FNR
        )
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCodeValue.toLong())
        assertFalse(body.skalHaVarsel)
    }

    @Test
    fun skal_ikke_ha_varsel_naar_kan_varsles_og_er_reservert() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)

        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        val digitalKontaktinfo = DigitalKontaktinfo(
            epostadresse = PERSON_EMAIL,
            kanVarsles = true,
            reservert = true,
            mobiltelefonnummer = PERSON_TLF,
            personident = ARBEIDSTAKER_FNR
        )
        `when`(dkifConsumer.kontaktinformasjon(anyString())).thenReturn(digitalKontaktinfo)

        val res: ResponseEntity<*> = kontaktinfoController.getKontaktinfo(ARBEIDSTAKER_FNR)
        val body = res.body as Kontaktinfo
        assertEquals(200, res.statusCodeValue.toLong())
        assertFalse(body.skalHaVarsel)
    }

}
