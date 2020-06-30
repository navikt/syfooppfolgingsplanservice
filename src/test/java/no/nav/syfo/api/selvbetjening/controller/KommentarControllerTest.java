package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.KommentarService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static org.mockito.Mockito.verify;

public class KommentarControllerTest extends AbstractRessursTilgangTest {

    @Inject
    private KommentarController kommentarController;

    @MockBean
    KommentarService kommentarService;
    @MockBean
    Metrikk metrikk;

    private static Long kommentarId = 1L;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);
    }

    @Test
    public void sletter_tiltak_som_bruker() {
        kommentarController.slettKommentar(kommentarId);

        verify(kommentarService).slettKommentar(kommentarId, ARBEIDSTAKER_FNR);
        verify(metrikk).tellHendelse("slett_kommentar");
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_slett_tiltak() {
        loggUtAlle(oidcRequestContextHolder);

        kommentarController.slettKommentar(kommentarId);
    }
}
