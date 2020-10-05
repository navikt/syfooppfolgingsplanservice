package no.nav.syfo.api.selvbetjening.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.OppfolgingsplanService
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import org.junit.*
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class ArbeidstakerOppfolgingsplanControllerTest : AbstractRessursTilgangTest() {
    @MockBean
    lateinit var oppfolgingsplanService: OppfolgingsplanService

    @MockBean
    lateinit var metrikk: Metrikk

    @Inject
    private lateinit var arbeidstakerOppfolgingsplanController: ArbeidstakerOppfolgingsplanController

    @Before
    fun setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR)
    }

    @Test
    fun henter_oppfolgingsplaner_som_arbeidstaker() {
        arbeidstakerOppfolgingsplanController.hentArbeidstakersOppfolgingsplaner()
        Mockito.verify(oppfolgingsplanService).hentAktorsOppfolgingsplaner(BrukerkontekstConstant.ARBEIDSTAKER, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("hent_oppfolgingsplan_at")
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        arbeidstakerOppfolgingsplanController.hentArbeidstakersOppfolgingsplaner()
    }

    @Test
    fun opprett_oppfolgingsplan_som_arbeidstaker() {
        val ressursId = 1L
        val rsOpprettOppfoelgingsdialog = RSOpprettOppfoelgingsdialog()
            .virksomhetsnummer(VIRKSOMHETSNUMMER)
        Mockito.`when`(oppfolgingsplanService.opprettOppfolgingsplan(rsOpprettOppfoelgingsdialog, ARBEIDSTAKER_FNR)).thenReturn(ressursId)
        val res = arbeidstakerOppfolgingsplanController.opprettOppfolgingsplanSomArbeidstaker(rsOpprettOppfoelgingsdialog)
        Mockito.verify(oppfolgingsplanService).opprettOppfolgingsplan(rsOpprettOppfoelgingsdialog, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("opprett_oppfolgingsplan_at")
        Assert.assertEquals(res, ressursId)
    }

    @Test(expected = RuntimeException::class)
    fun opprett_oppfolgingsplan_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        val rsOpprettOppfoelgingsdialog = RSOpprettOppfoelgingsdialog()
        arbeidstakerOppfolgingsplanController.opprettOppfolgingsplanSomArbeidstaker(rsOpprettOppfoelgingsdialog)
    }
}
