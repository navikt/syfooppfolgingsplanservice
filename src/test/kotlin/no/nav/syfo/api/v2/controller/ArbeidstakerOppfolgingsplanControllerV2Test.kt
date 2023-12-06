package no.nav.syfo.api.v2.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.v2.domain.oppfolgingsplan.OpprettOppfolgingsplanRequest
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.service.ArbeidsforholdService
import no.nav.syfo.service.OppfolgingsplanService
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_AKTORID
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.testhelper.oppfolgingsplanGodkjentTvang
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class ArbeidstakerOppfolgingsplanControllerV2Test : AbstractRessursTilgangTest() {
    @MockBean
    lateinit var oppfolgingsplanService: OppfolgingsplanService

    @MockBean
    lateinit var arbeidsforholdService: ArbeidsforholdService

    @MockBean
    lateinit var pdlConsumer: PdlConsumer

    @MockBean
    lateinit var metrikk: Metrikk

    @Inject
    private lateinit var arbeidstakerOppfolgingsplanController: ArbeidstakerOppfolgingsplanControllerV2

    @Before
    fun setup() {
        tokenValidationTestUtil.logInAsUser(ARBEIDSTAKER_FNR)
    }

    @Test
    fun henter_oppfolgingsplaner_som_arbeidstaker() {
        `when`(oppfolgingsplanService.arbeidstakersOppfolgingsplaner(ARBEIDSTAKER_FNR)).thenReturn(listOf(oppfolgingsplanGodkjentTvang()))
        `when`(pdlConsumer.fnr(ARBEIDSTAKER_AKTORID)).thenReturn(ARBEIDSTAKER_FNR)
        `when`(pdlConsumer.fnr(LEDER_AKTORID)).thenReturn(LEDER_FNR)
        arbeidstakerOppfolgingsplanController.hentArbeidstakersOppfolgingsplaner()
        verify(oppfolgingsplanService).arbeidstakersOppfolgingsplaner(ARBEIDSTAKER_FNR)
        verify(arbeidsforholdService).arbeidstakersStillingerForOrgnummer(ARBEIDSTAKER_FNR, listOf(VIRKSOMHETSNUMMER))
        verify(metrikk).tellHendelse("hent_oppfolgingsplan_at")
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker() {
        tokenValidationTestUtil.logout()
        arbeidstakerOppfolgingsplanController.hentArbeidstakersOppfolgingsplaner()
    }

    @Test
    fun opprett_oppfolgingsplan_som_arbeidstaker() {
        val ressursId = 1L
        val opprettOppfolgingsplan = OpprettOppfolgingsplanRequest("", VIRKSOMHETSNUMMER)
        `when`(oppfolgingsplanService.opprettOppfolgingsplan(ARBEIDSTAKER_FNR, opprettOppfolgingsplan.virksomhetsnummer, ARBEIDSTAKER_FNR)).thenReturn(ressursId)
        val res = arbeidstakerOppfolgingsplanController.opprettOppfolgingsplanSomArbeidstaker(opprettOppfolgingsplan)
        verify(oppfolgingsplanService).opprettOppfolgingsplan(ARBEIDSTAKER_FNR, opprettOppfolgingsplan.virksomhetsnummer, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("opprett_oppfolgingsplan_at")
        assertEquals(res, ressursId)
    }

    @Test(expected = RuntimeException::class)
    fun opprett_oppfolgingsplan_ikke_innlogget_bruker() {
        tokenValidationTestUtil.logout()
        val opprettOppfolgingsplan = OpprettOppfolgingsplanRequest("", VIRKSOMHETSNUMMER)
        arbeidstakerOppfolgingsplanController.opprettOppfolgingsplanSomArbeidstaker(opprettOppfolgingsplan)
    }
}
