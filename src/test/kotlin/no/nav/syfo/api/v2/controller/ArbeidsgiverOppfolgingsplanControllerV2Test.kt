package no.nav.syfo.api.v2.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v2.domain.oppfolgingsplan.OpprettOppfolgingsplanRequest
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.service.ArbeidsforholdService
import no.nav.syfo.service.OppfolgingsplanService
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import no.nav.syfo.testhelper.oppfolgingsplanGodkjentTvang
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

class ArbeidsgiverOppfolgingsplanControllerV2Test : AbstractRessursTilgangTest() {

    @MockBean
    lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @MockBean
    lateinit var oppfolgingsplanService: OppfolgingsplanService

    @MockBean
    lateinit var arbeidsforholdService: ArbeidsforholdService

    @MockBean
    lateinit var pdlConsumer: PdlConsumer

    @MockBean
    lateinit var metrikk: Metrikk

    @Inject
    private lateinit var arbeidsgiverOppfolgingsplanController: ArbeidsgiverOppfolgingsplanControllerV2

    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    @Before
    fun setup() {
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
    }

    @Test(expected = RuntimeException::class)
    fun hent_oppfolgingsplaner_finner_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        arbeidsgiverOppfolgingsplanController.hentArbeidsgiversOppfolgingsplanerPaFnr(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
    }

    @Test
    fun hent_oppfolgingsplaner_som_arbeidgiver_pa_fnr() {
        `when`(oppfolgingsplanService.arbeidsgiversOppfolgingsplanerPaFnr(LEDER_FNR, ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)).thenReturn(listOf(oppfolgingsplanGodkjentTvang()))
        `when`(pdlConsumer.fnr(UserConstants.ARBEIDSTAKER_AKTORID)).thenReturn(ARBEIDSTAKER_FNR)
        `when`(pdlConsumer.fnr(UserConstants.LEDER_AKTORID)).thenReturn(LEDER_FNR)
        arbeidsgiverOppfolgingsplanController.hentArbeidsgiversOppfolgingsplanerPaFnr(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        verify(oppfolgingsplanService).arbeidsgiversOppfolgingsplanerPaFnr(LEDER_FNR, ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        verify(arbeidsforholdService).arbeidstakersStillingerForOrgnummer(ARBEIDSTAKER_FNR, listOf(VIRKSOMHETSNUMMER))
        verify(metrikk).tellHendelse("hent_oppfolgingsplan_ag")
    }

    @Test
    fun opprett_oppfolgingsplan_som_arbeidsgiver() {
        val ressursId = 1L
        val opprettOppfolgingsplan = OpprettOppfolgingsplanRequest(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        `when`(narmesteLederConsumer.erNaermesteLederForAnsatt(LEDER_FNR, ARBEIDSTAKER_FNR)).thenReturn(true)
        `when`(oppfolgingsplanService.opprettOppfolgingsplan(LEDER_FNR, VIRKSOMHETSNUMMER, ARBEIDSTAKER_FNR)).thenReturn(ressursId)
        val res = arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(opprettOppfolgingsplan)
        verify(metrikk).tellHendelse("opprett_oppfolgingsplan_ag")
        Assert.assertEquals(res, ressursId)
    }

    @Test(expected = ForbiddenException::class)
    fun opprett_oppfolgingsplan_som_arbeidsgiver_ikke_leder_arbeidstaker() {
        val opprettOppfolgingsplan = OpprettOppfolgingsplanRequest(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        `when`(narmesteLederConsumer.erNaermesteLederForAnsatt(LEDER_FNR, ARBEIDSTAKER_FNR)).thenReturn(false)
        arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(opprettOppfolgingsplan)
    }

    @Test(expected = RuntimeException::class)
    fun opprett_oppfolgingsplan_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        val opprettOppfolgingsplan = OpprettOppfolgingsplanRequest(ARBEIDSTAKER_FNR, VIRKSOMHETSNUMMER)
        arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(opprettOppfolgingsplan)
    }
}
