package no.nav.syfo.api.selvbetjening.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.service.OppfolgingsplanService
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_AKTORID
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

class ArbeidsgiverOppfolgingsplanControllerTest : AbstractRessursTilgangTest() {
    @MockBean
    lateinit var pdlConsumer: PdlConsumer

    @MockBean
    lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @MockBean
    lateinit var oppfolgingsplanService: OppfolgingsplanService

    @MockBean
    lateinit var metrikk: Metrikk

    @Inject
    private lateinit var arbeidsgiverOppfolgingsplanController: ArbeidsgiverOppfolgingsplanController

    @Before
    fun setup() {
        loggInnBruker(contextHolder, LEDER_FNR)
        mockPdlConsumer()
    }

    @Test
    fun hent_oppfolgingsplaner_som_arbeidsgiver() {
        arbeidsgiverOppfolgingsplanController.hentArbeidsgiversOppfolgingsplaner()
        Mockito.verify(oppfolgingsplanService).hentAktorsOppfolgingsplaner(BrukerkontekstConstant.ARBEIDSGIVER, LEDER_FNR)
        Mockito.verify(metrikk).tellHendelse("hent_oppfolgingsplan_ag")
    }

    @Test(expected = RuntimeException::class)
    fun hent_oppfolgingsplaner_finner_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        arbeidsgiverOppfolgingsplanController.hentArbeidsgiversOppfolgingsplaner()
    }

    @Test
    fun opprett_oppfolgingsplan_som_arbeidsgiver() {
        val ressursId = 1L
        val rsOpprettOppfoelgingsdialog = RSOpprettOppfoelgingsdialog()
            .sykmeldtFnr(ARBEIDSTAKER_FNR)
            .virksomhetsnummer(VIRKSOMHETSNUMMER)
        Mockito.`when`(narmesteLederConsumer.erNaermesteLederForAnsatt(LEDER_FNR, ARBEIDSTAKER_FNR)).thenReturn(true)
        Mockito.`when`(oppfolgingsplanService.opprettOppfolgingsplan(rsOpprettOppfoelgingsdialog, LEDER_FNR)).thenReturn(ressursId)
        val res = arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(rsOpprettOppfoelgingsdialog)
        Mockito.verify(metrikk).tellHendelse("opprett_oppfolgingsplan_ag")
        Assert.assertEquals(res, ressursId)
    }

    @Test(expected = ForbiddenException::class)
    fun opprett_oppfolgingsplan_som_arbeidsgiver_ikke_leder_arbeidstaker() {
        val rsOpprettOppfoelgingsdialog = RSOpprettOppfoelgingsdialog()
            .sykmeldtFnr(LEDER_FNR)
            .virksomhetsnummer(VIRKSOMHETSNUMMER)
        Mockito.`when`(narmesteLederConsumer.erNaermesteLederForAnsatt(LEDER_FNR, ARBEIDSTAKER_FNR)).thenReturn(false)
        arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(rsOpprettOppfoelgingsdialog)
    }

    @Test(expected = RuntimeException::class)
    fun opprett_oppfolgingsplan_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        val rsOpprettOppfoelgingsdialog = RSOpprettOppfoelgingsdialog()
        arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(rsOpprettOppfoelgingsdialog)
    }

    private fun mockPdlConsumer() {
        Mockito.`when`(pdlConsumer.fnr(ARBEIDSTAKER_AKTORID)).thenReturn(ARBEIDSTAKER_FNR)
        Mockito.`when`(pdlConsumer.aktorid(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID)
        Mockito.`when`(pdlConsumer.fnr(LEDER_AKTORID)).thenReturn(LEDER_FNR)
        Mockito.`when`(pdlConsumer.aktorid(LEDER_FNR)).thenReturn(LEDER_AKTORID)
    }
}