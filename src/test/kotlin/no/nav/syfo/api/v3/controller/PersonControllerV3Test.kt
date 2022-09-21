package no.nav.syfo.api.v3.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v2.domain.Person
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.pdl.PdlHentPerson
import no.nav.syfo.pdl.PdlPerson
import no.nav.syfo.pdl.PdlPersonNavn
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import javax.inject.Inject

class PersonControllerV3Test : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var pdlConsumer: PdlConsumer

    @Inject
    private lateinit var personController: PersonControllerV3

    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    @Test
    fun narmesteLeder_ansatt_ok() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(pdlConsumer.person(ARBEIDSTAKER_FNR))
            .thenReturn(PdlHentPerson(PdlPerson(listOf(PdlPersonNavn("Test", "Junior", "Testesen")), emptyList())))
        val res: ResponseEntity<*> = personController.getPerson(ARBEIDSTAKER_FNR)
        val body = res.body as Person
        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(ARBEIDSTAKER_FNR, body.fnr)
        assertEquals("Test Junior Testesen", body.navn)
    }

    @Test
    fun narmesteLeder_self_ok() {
        loggInnBrukerTokenX(contextHolder, ARBEIDSTAKER_FNR, oppfolgingsplanClientId, tokenxIdp)
        `when`(pdlConsumer.person(ARBEIDSTAKER_FNR))
            .thenReturn(PdlHentPerson(PdlPerson(listOf(PdlPersonNavn("Test", "Junior", "Testesen")), emptyList())))
        val res: ResponseEntity<*> = personController.getPerson(ARBEIDSTAKER_FNR)
        val body = res.body as Person
        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(ARBEIDSTAKER_FNR, body.fnr)
        assertEquals("Test Junior Testesen", body.navn)
    }

    @Test
    fun narmesteLeder_noContent() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(pdlConsumer.person(ARBEIDSTAKER_FNR))
            .thenReturn(null)
        val res: ResponseEntity<*> = personController.getPerson(ARBEIDSTAKER_FNR)
        assertEquals(404, res.statusCodeValue.toLong())
    }

    @Test
    fun narmesteLeder_forbidden() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(false)

        val res: ResponseEntity<*> = personController.getPerson(ARBEIDSTAKER_FNR)
        assertEquals(403, res.statusCodeValue.toLong())
    }
}
