package no.nav.syfo.api.v3.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v2.domain.NarmesteLeder
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.model.NaermesteLederStatus
import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class NarmesteLederControllerV3Test : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    @Inject
    private lateinit var narmesteLederController: NarmesteLederControllerV3

    private val naermesteleder = Naermesteleder()
        .naermesteLederFnr(LEDER_FNR)
        .orgnummer(VIRKSOMHETSNUMMER)
        .naermesteLederStatus(NaermesteLederStatus().erAktiv(true).aktivFom(LocalDate.now()).aktivTom(LocalDate.now()))
        .navn("Test Testesen")

    @Test
    fun narmesteLeder_ansatt_ok() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER))
            .thenReturn(Optional.of(naermesteleder))
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        val body = res.body as NarmesteLeder
        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(naermesteleder.naermesteLederFnr, body.fnr)
        assertEquals(naermesteleder.orgnummer, body.virksomhetsnummer)
        assertEquals(naermesteleder.naermesteLederStatus.erAktiv, body.erAktiv)
        assertEquals(naermesteleder.naermesteLederStatus.aktivFom, body.aktivFom)
        assertEquals(naermesteleder.naermesteLederStatus.aktivTom, body.aktivTom)
    }

    @Test
    fun narmesteLeder_self_ok() {
        loggInnBrukerTokenX(contextHolder, ARBEIDSTAKER_FNR, oppfolgingsplanClientId, tokenxIdp)
        `when`(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER))
            .thenReturn(Optional.of(naermesteleder))
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        val body = res.body as NarmesteLeder
        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(naermesteleder.naermesteLederFnr, body.fnr)
        assertEquals(naermesteleder.orgnummer, body.virksomhetsnummer)
        assertEquals(naermesteleder.naermesteLederStatus.erAktiv, body.erAktiv)
        assertEquals(naermesteleder.naermesteLederStatus.aktivFom, body.aktivFom)
        assertEquals(naermesteleder.naermesteLederStatus.aktivTom, body.aktivTom)
    }

    @Test
    fun narmesteLeder_noContent() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)).thenReturn(Optional.empty())
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        assertEquals(204, res.statusCodeValue.toLong())
    }

    @Test
    fun narmesteLeder_forbidden() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(false)
        val res: ResponseEntity<*> = narmesteLederController.getNarmesteLeder(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        assertEquals(403, res.statusCodeValue.toLong())
    }
}
