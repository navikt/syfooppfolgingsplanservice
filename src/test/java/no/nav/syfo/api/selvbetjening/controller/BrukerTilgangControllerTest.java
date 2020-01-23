package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.api.selvbetjening.domain.RSTilgang;
import no.nav.syfo.service.BrukertilgangService;
import no.nav.syfo.service.PersonService;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.api.selvbetjening.controller.BrukerTilgangController.IKKE_TILGANG_GRUNN_DISKRESJONSMERKET;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static no.nav.syfo.testhelper.UserConstants.LEDER_FNR;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class BrukerTilgangControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    BrukertilgangService brukertilgangService;
    @MockBean
    PersonService personService;

    @Inject
    private BrukerTilgangController brukerTilgangController;

    @Test
    public void sjekk_tilgang_til_bruker() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR);

        when(personService.erDiskresjonsmerket(ARBEIDSTAKER_FNR)).thenReturn(false);
        when(brukertilgangService.tilgangTilOppslattIdent(LEDER_FNR, ARBEIDSTAKER_FNR)).thenReturn(true);

        RSTilgang exp = new RSTilgang()
                .harTilgang(true);
        RSTilgang res = brukerTilgangController.harTilgang(ARBEIDSTAKER_FNR);

        assertEquals(res, exp);
    }

    @Test
    public void sjekk_tilgang_til_seg_selv() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);

        when(personService.erDiskresjonsmerket(ARBEIDSTAKER_FNR)).thenReturn(false);
        when(brukertilgangService.tilgangTilOppslattIdent(ARBEIDSTAKER_FNR, ARBEIDSTAKER_FNR)).thenReturn(true);

        RSTilgang exp = new RSTilgang()
                .harTilgang(true);
        RSTilgang res = brukerTilgangController.harTilgang(null);

        assertEquals(res, exp);
    }

    @Test
    public void sjekk_tilgang_til_bruker_diskresjonsmerket() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR);

        when(personService.erDiskresjonsmerket(ARBEIDSTAKER_FNR)).thenReturn(true);
        when(brukertilgangService.tilgangTilOppslattIdent(LEDER_FNR, ARBEIDSTAKER_FNR)).thenReturn(true);

        RSTilgang exp = new RSTilgang()
                .harTilgang(false)
                .ikkeTilgangGrunn(IKKE_TILGANG_GRUNN_DISKRESJONSMERKET);
        RSTilgang res = brukerTilgangController.harTilgang(ARBEIDSTAKER_FNR);

        assertEquals(res, exp);
    }

    @Test
    public void sjekk_tilgang_til_seg_selv_diskresjonsmerket() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);

        when(personService.erDiskresjonsmerket(ARBEIDSTAKER_FNR)).thenReturn(true);
        when(brukertilgangService.tilgangTilOppslattIdent(ARBEIDSTAKER_FNR, ARBEIDSTAKER_FNR)).thenReturn(true);

        RSTilgang exp = new RSTilgang()
                .harTilgang(false)
                .ikkeTilgangGrunn(IKKE_TILGANG_GRUNN_DISKRESJONSMERKET);
        RSTilgang res = brukerTilgangController.harTilgang(null);

        assertEquals(res, exp);
    }


    @Test(expected = ForbiddenException.class)
    public void sjekk_tilgang_til_bruker_ikke_tilgang() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR);

        when(brukertilgangService.tilgangTilOppslattIdent(LEDER_FNR, ARBEIDSTAKER_FNR)).thenReturn(false);

        brukerTilgangController.harTilgang(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void sjekk_tilgang_til_seg_selv_ikke_tilgang() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);

        when(brukertilgangService.tilgangTilOppslattIdent(ARBEIDSTAKER_FNR, ARBEIDSTAKER_FNR)).thenReturn(false);

        brukerTilgangController.harTilgang(null);
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        brukerTilgangController.harTilgang(null);
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_ved_oppslag() {
        loggUtAlle(oidcRequestContextHolder);

        brukerTilgangController.harTilgang("");
    }
}
