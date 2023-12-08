package no.nav.syfo.api.v3.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v2.domain.Virksomhet
import no.nav.syfo.ereg.EregConsumer
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import javax.inject.Inject

class VirksomhetControllerV3Test : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var eregConsumer: EregConsumer

    @Inject
    private lateinit var virksomhetController: VirksomhetControllerV3

    private val virksomhetsNavn = "Tull og fanteri AS"

    private val virksomhet = Virksomhet(VIRKSOMHETSNUMMER, virksomhetsNavn)

    @Test
    fun virksomhet_ok() {
        tokenValidationTestUtil.logInAsUser(LEDER_FNR)
        `when`(eregConsumer.virksomhetsnavn(VIRKSOMHETSNUMMER))
            .thenReturn(virksomhetsNavn)
        val res: ResponseEntity<*> = virksomhetController.getVirksomhet(VIRKSOMHETSNUMMER)
        val body = res.body as Virksomhet
        assertEquals(200, res.statusCode.value().toLong())
        assertEquals(virksomhet.virksomhetsnummer, body.virksomhetsnummer)
        assertEquals(virksomhet.navn, body.navn)
    }

    @Test
    fun virksomhet_invalid_virksomhetsnummer() {
        tokenValidationTestUtil.logInAsUser(LEDER_FNR)
        `when`(eregConsumer.virksomhetsnavn(VIRKSOMHETSNUMMER))
            .thenReturn(virksomhetsNavn)
        val res: ResponseEntity<*> = virksomhetController.getVirksomhet("12345678")
        assertNull(res.body)
        assertEquals(418, res.statusCodeValue.toLong())
    }
}
