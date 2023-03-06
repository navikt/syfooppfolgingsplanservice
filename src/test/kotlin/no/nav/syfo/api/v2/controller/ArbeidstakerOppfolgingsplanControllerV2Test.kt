package no.nav.syfo.api.v2.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.service.BrukerkontekstConstant
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.OppfolgingsplanService
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import no.nav.syfo.testhelper.loggInnBrukerTokenX
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
    lateinit var metrikk: Metrikk

    @Inject
    private lateinit var arbeidstakerOppfolgingsplanController: ArbeidstakerOppfolgingsplanControllerV2

    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String


    @Before
    fun setup() {
        loggInnBrukerTokenX(contextHolder, ARBEIDSTAKER_FNR, oppfolgingsplanClientId, tokenxIdp)
    }

    @Test
    fun henter_oppfolgingsplaner_som_arbeidstaker() {
        arbeidstakerOppfolgingsplanController.hentArbeidstakersOppfolgingsplaner()
        verify(oppfolgingsplanService).hentAktorsOppfolgingsplaner(BrukerkontekstConstant.ARBEIDSTAKER, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("hent_oppfolgingsplan_at")
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        arbeidstakerOppfolgingsplanController.hentArbeidstakersOppfolgingsplaner()
    }

    @Test
    fun opprett_oppfolgingsplan_som_arbeidstaker() {
        val ressursId = 1L
        val rsOpprettOppfoelgingsdialog = RSOpprettOppfoelgingsdialog()
            .virksomhetsnummer(VIRKSOMHETSNUMMER)
        `when`(oppfolgingsplanService.opprettOppfolgingsplan(rsOpprettOppfoelgingsdialog, ARBEIDSTAKER_FNR)).thenReturn(ressursId)
        val res = arbeidstakerOppfolgingsplanController.opprettOppfolgingsplanSomArbeidstaker(rsOpprettOppfoelgingsdialog)
        verify(oppfolgingsplanService).opprettOppfolgingsplan(rsOpprettOppfoelgingsdialog, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("opprett_oppfolgingsplan_at")
        assertEquals(res, ressursId)
    }

    @Test(expected = RuntimeException::class)
    fun opprett_oppfolgingsplan_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        val rsOpprettOppfoelgingsdialog = RSOpprettOppfoelgingsdialog()
        arbeidstakerOppfolgingsplanController.opprettOppfolgingsplanSomArbeidstaker(rsOpprettOppfoelgingsdialog)
    }
}
