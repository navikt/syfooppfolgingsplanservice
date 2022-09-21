package no.nav.syfo.api.v3.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.selvbetjening.controller.NarmesteLedereControllerV2
import no.nav.syfo.api.v2.domain.NarmesteLeder
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.model.NaermesteLederStatus
import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.narmesteleder.NarmesteLedereConsumer
import no.nav.syfo.testhelper.OidcTestHelper
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class NarmesteLedereControllerV3Test : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var brukertilgangConsumer: BrukertilgangConsumer

    @MockBean
    lateinit var narmesteLedereConsumer: NarmesteLedereConsumer

    @Inject
    private lateinit var narmesteLedereController: NarmesteLedereControllerV2

    private val naermesteleder = Naermesteleder()
        .naermesteLederFnr(UserConstants.LEDER_FNR)
        .orgnummer(UserConstants.VIRKSOMHETSNUMMER)
        .naermesteLederStatus(NaermesteLederStatus().erAktiv(true).aktivFom(LocalDate.now()).aktivTom(LocalDate.now()))
        .navn("Test Testesen")

    private val naermesteledere = listOf<Naermesteleder>(naermesteleder)

    @Test
    fun narmesteLeder_ansatt_ok() {
        OidcTestHelper.loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(narmesteLedereConsumer.narmesteLedere(ARBEIDSTAKER_FNR))
            .thenReturn(Optional.of(naermesteledere))
        val res: ResponseEntity<*> = narmesteLedereController.getNarmesteLedere(ARBEIDSTAKER_FNR)
        val body = res.body as List<NarmesteLeder>
        val actualNarmesteLeder = body[0]
        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(naermesteleder.naermesteLederFnr, actualNarmesteLeder.fnr)
        assertEquals(naermesteleder.orgnummer, actualNarmesteLeder.virksomhetsnummer)
        assertEquals(naermesteleder.naermesteLederStatus.erAktiv, actualNarmesteLeder.erAktiv)
        assertEquals(naermesteleder.naermesteLederStatus.aktivFom, actualNarmesteLeder.aktivFom)
        assertEquals(naermesteleder.naermesteLederStatus.aktivTom, actualNarmesteLeder.aktivTom)
    }

    @Test
    fun narmesteLeder_self_ok() {
        OidcTestHelper.loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
        `when`(narmesteLedereConsumer.narmesteLedere(ARBEIDSTAKER_FNR))
            .thenReturn(Optional.of(listOf(naermesteleder)))
        val res: ResponseEntity<*> = narmesteLedereController.getNarmesteLedere(ARBEIDSTAKER_FNR)
        val body = res.body as List<NarmesteLeder>
        val actualNarmesteLeder = body[0]
        assertEquals(200, res.statusCodeValue.toLong())
        assertEquals(naermesteleder.naermesteLederFnr, actualNarmesteLeder.fnr)
        assertEquals(naermesteleder.orgnummer, actualNarmesteLeder.virksomhetsnummer)
        assertEquals(naermesteleder.naermesteLederStatus.erAktiv, actualNarmesteLeder.erAktiv)
        assertEquals(naermesteleder.naermesteLederStatus.aktivFom, actualNarmesteLeder.aktivFom)
        assertEquals(naermesteleder.naermesteLederStatus.aktivTom, actualNarmesteLeder.aktivTom)
    }

    @Test
    fun narmesteLeder_noContent() {
        OidcTestHelper.loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(narmesteLedereConsumer.narmesteLedere(ARBEIDSTAKER_FNR)).thenReturn(Optional.empty())
        val res: ResponseEntity<*> = narmesteLedereController.getNarmesteLedere(ARBEIDSTAKER_FNR)
        assertEquals(204, res.statusCodeValue.toLong())
    }

    @Test
    fun narmesteLeder_forbidden() {
        OidcTestHelper.loggInnBruker(contextHolder, UserConstants.LEDER_FNR)
        `when`(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(false)
        val res: ResponseEntity<*> = narmesteLedereController.getNarmesteLedere(ARBEIDSTAKER_FNR)
        assertEquals(403, res.statusCodeValue.toLong())
    }
}
