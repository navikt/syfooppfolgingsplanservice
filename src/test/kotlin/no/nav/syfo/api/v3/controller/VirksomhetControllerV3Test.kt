package no.nav.syfo.api.v3.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v2.domain.Virksomhet
import no.nav.syfo.ereg.EregConsumer
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import javax.inject.Inject

class VirksomhetControllerV3Test : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var eregConsumer: EregConsumer

    @Inject
    private lateinit var virksomhetController: VirksomhetControllerV3

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    private val virksomhetsNavn = "Tull og fanteri AS"

    private val virksomhet = Virksomhet(VIRKSOMHETSNUMMER, virksomhetsNavn)

    @Test
    fun virksomhet_ok() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId)
        `when`(eregConsumer.virksomhetsnavn(VIRKSOMHETSNUMMER))
            .thenReturn(virksomhetsNavn)
        val res: ResponseEntity<*> = virksomhetController.getVirksomhet(VIRKSOMHETSNUMMER)
        val body = res.body as Virksomhet
        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(virksomhet.virksomhetsnummer, body.virksomhetsnummer)
        assertEquals(virksomhet.navn, body.navn)
    }

}
