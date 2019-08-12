package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.OppfoelgingsdialogService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;

import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSTAKER;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArbeidstakerOppfolgingsplanControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    OppfoelgingsdialogService oppfoelgingsdialogService;
    @MockBean
    Metrikk metrikk;

    @Inject
    private ArbeidstakerOppfolgingsplanController arbeidstakerOppfolgingsplanController;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);
    }

    @Test
    public void henter_oppfolgingsplaner_som_arbeidstaker() {
        arbeidstakerOppfolgingsplanController.hentArbeidstakersOppfolgingsplaner();

        verify(oppfoelgingsdialogService).hentAktoersOppfoelgingsdialoger(ARBEIDSTAKER, ARBEIDSTAKER_FNR);
        verify(metrikk).tellHendelse("hent_oppfolgingsplan_at");
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        arbeidstakerOppfolgingsplanController.hentArbeidstakersOppfolgingsplaner();
    }

    @Test
    public void opprett_oppfolgingsplan_som_arbeidstaker() {
        Long ressursId = 1L;

        RSOpprettOppfoelgingsdialog rsOpprettOppfoelgingsdialog = new RSOpprettOppfoelgingsdialog()
                .virksomhetsnummer(VIRKSOMHETSNUMMER);

        when(oppfoelgingsdialogService.opprettOppfoelgingsdialog(rsOpprettOppfoelgingsdialog, ARBEIDSTAKER_FNR)).thenReturn(ressursId);

        Long res = arbeidstakerOppfolgingsplanController.opprettOppfolgingsplanSomArbeidstaker(rsOpprettOppfoelgingsdialog);

        verify(oppfoelgingsdialogService).opprettOppfoelgingsdialog(rsOpprettOppfoelgingsdialog, ARBEIDSTAKER_FNR);
        verify(metrikk).tellHendelse("opprett_oppfolgingsplan_at");

        assertEquals(res, ressursId);
    }

    @Test(expected = RuntimeException.class)
    public void opprett_oppfolgingsplan_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        RSOpprettOppfoelgingsdialog rsOpprettOppfoelgingsdialog = new RSOpprettOppfoelgingsdialog();

        arbeidstakerOppfolgingsplanController.opprettOppfolgingsplanSomArbeidstaker(rsOpprettOppfoelgingsdialog);
    }
}
