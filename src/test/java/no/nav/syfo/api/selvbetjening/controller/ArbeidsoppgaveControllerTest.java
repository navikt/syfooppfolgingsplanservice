package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.service.ArbeidsoppgaveService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static org.mockito.Mockito.verify;

public class ArbeidsoppgaveControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    ArbeidsoppgaveService arbeidsoppgaveService;

    @Inject
    private ArbeidsoppgaveController arbeidsoppgaveController;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);
    }

    @Test
    public void sletter_arbeidsoppgave_som_bruker() {
        arbeidsoppgaveController.slettArbeidsoppgave(1L);
        verify(arbeidsoppgaveService).slettArbeidsoppgave(1L, ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        arbeidsoppgaveController.slettArbeidsoppgave(1L);
    }
}
