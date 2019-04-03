package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

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
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        arbeidstakerOppfolgingsplanController.hentArbeidstakersOppfolgingsplaner();
    }
}
