package no.nav.syfo.api.intern.ressurs;

import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.service.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeileder;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static no.nav.syfo.testhelper.UserConstants.VEILEDER_ID;
import static org.springframework.http.HttpStatus.*;

public class OppfoelgingsdialogRessursTest extends AbstractRessursTilgangTest {

    @Inject
    private OppfoelgingsdialogRessurs oppfoelgingsdialogRessurs;

    @MockBean
    private AktoerService aktoerService;
    @MockBean
    private VeilederOppgaverService veilederOppgaverService;
    @MockBean
    private BrukerprofilService brukerprofilService;
    @MockBean
    private OrganisasjonService organisasjonService;
    @MockBean
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;


    @Before
    public void setup() {
        loggInnVeileder(oidcRequestContextHolder, VEILEDER_ID);
    }

    @Test
    public void historikk_har_tilgang() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, OK);

        oppfoelgingsdialogRessurs.historikk(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void historikk_har_ikke_tilgang() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, FORBIDDEN);

        oppfoelgingsdialogRessurs.historikk(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void historikk_annen_tilgangsfeil() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, INTERNAL_SERVER_ERROR);

        oppfoelgingsdialogRessurs.historikk(ARBEIDSTAKER_FNR);
    }

    @Test
    public void hentDialoger_har_tilgang() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, OK);

        oppfoelgingsdialogRessurs.hentDialoger(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void hentDialoger_har_ikke_tilgang() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, FORBIDDEN);

        oppfoelgingsdialogRessurs.hentDialoger(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void hentDialoger_annen_tilgangsfeil() {
        mockSvarFraTilgangTilBruker(ARBEIDSTAKER_FNR, INTERNAL_SERVER_ERROR);

        oppfoelgingsdialogRessurs.hentDialoger(ARBEIDSTAKER_FNR);
    }

}
