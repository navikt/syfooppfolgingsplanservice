package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.OppfoelgingsdialogService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;

import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSGIVER;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.LEDER_FNR;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

public class ArbeidsgiverOppfolgingsplanControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    AktoerService aktoerService;
    @MockBean
    OppfoelgingsdialogService oppfoelgingsdialogService;
    @MockBean
    Metrikk metrikk;

    @Inject
    private ArbeidsgiverOppfolgingsplanController arbeidsgiverOppfolgingsplanController;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR);
    }

    @Test
    public void hent_oppfolgingsplaner_som_arbeidsgiver() {
        arbeidsgiverOppfolgingsplanController.hentArbeidsgiversOppfolgingsplaner();

        verify(oppfoelgingsdialogService).hentAktoersOppfoelgingsdialoger(ARBEIDSGIVER, LEDER_FNR);
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void hent_oppfolgingsplaner_finner_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        arbeidsgiverOppfolgingsplanController.hentArbeidsgiversOppfolgingsplaner();
    }
}
