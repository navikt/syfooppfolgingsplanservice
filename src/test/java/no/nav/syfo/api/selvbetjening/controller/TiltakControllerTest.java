package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.api.selvbetjening.domain.RSKommentar;
import no.nav.syfo.domain.Kommentar;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.KommentarService;
import no.nav.syfo.service.TiltakService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;

import static no.nav.syfo.api.selvbetjening.controller.TiltakController.rsKommentar2kommentar;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static no.nav.syfo.util.MapUtil.map;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TiltakControllerTest extends AbstractRessursTilgangTest {

    @Inject
    private TiltakController tiltakController;

    @MockBean
    KommentarService kommentarService;
    @MockBean
    TiltakService tiltakService;
    @MockBean
    Metrikk metrikk;

    private static Long tiltakId = 1L;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);
    }

    @Test
    public void sletter_tiltak_som_bruker() {
        tiltakController.slettTiltak(tiltakId);
        verify(tiltakService).slettTiltak(tiltakId, ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_slett_tiltak() {
        loggUtAlle(oidcRequestContextHolder);

        tiltakController.slettTiltak(tiltakId);
    }

    @Test
    public void lagrer_kommentar_som_bruker() {
        RSKommentar rsKommentar = new RSKommentar()
                .tekst("Kommentar");

        Kommentar kommentar = map(rsKommentar, rsKommentar2kommentar);

        Long kommentarId = 1L;
        when(kommentarService.lagreKommentar(tiltakId, kommentar, ARBEIDSTAKER_FNR)).thenReturn(kommentarId);

        Long res = tiltakController.lagreKommentar(tiltakId, rsKommentar);


        verify(kommentarService).lagreKommentar(eq(tiltakId), any(Kommentar.class), eq(ARBEIDSTAKER_FNR));
        verify(metrikk).tellHendelse("lagre_kommentar");

        assertEquals(res, kommentarId);
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_lagre_kommentar() {
        loggUtAlle(oidcRequestContextHolder);
        RSKommentar kommentar = new RSKommentar();

        tiltakController.lagreKommentar(tiltakId, kommentar);
    }
}
