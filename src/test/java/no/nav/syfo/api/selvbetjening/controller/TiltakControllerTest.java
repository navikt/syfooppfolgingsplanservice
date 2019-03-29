package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.service.TiltakService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static org.mockito.Mockito.verify;

public class TiltakControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    TiltakService tiltakService;

    @Inject
    private TiltakController tiltakController;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);
    }

    @Test
    public void sletter_arbeidsoppgave_som_bruker() {
        tiltakController.slettTiltak(1L);
        verify(tiltakService).slettTiltak(1L, ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        tiltakController.slettTiltak(1L);
    }
}
