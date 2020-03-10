package no.nav.syfo.api.intern.controller;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.repository.dao.OppfolgingsplanDAO;
import no.nav.syfo.service.*;
import org.junit.*;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.text.ParseException;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.springframework.http.HttpStatus.*;

public class OppfolgingsplanInternControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    private AktorregisterConsumer aktorregisterConsumer;
    @MockBean
    private OppfolgingsplanDAO oppfolgingsplanDAO;
    @MockBean
    private BrukerprofilService brukerprofilService;
    @MockBean
    private OrganisasjonService organisasjonService;

    @Inject
    private OppfolgingsplanInternController oppfolgingsplanInternController;

    @Before
    public void setup() throws ParseException {
        loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID);
    }

    @After
    public void tearDown() {
        loggUtAlle(oidcRequestContextHolder);
    }

    @Test
    public void getHistoryHasAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, OK);

        oppfolgingsplanInternController.getHistorikk(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void getHistoryNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, FORBIDDEN);

        oppfolgingsplanInternController.getHistorikk(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void getHistoryServerError() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, INTERNAL_SERVER_ERROR);

        oppfolgingsplanInternController.getHistorikk(ARBEIDSTAKER_FNR);
    }

    @Test
    public void getOppfolgingsplanerHasAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, OK);

        oppfolgingsplanInternController.getOppfolgingsplaner(ARBEIDSTAKER_FNR);
    }

    @Test(expected = ForbiddenException.class)
    public void getOppfolgingsplanerNoAccess() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, FORBIDDEN);

        oppfolgingsplanInternController.getOppfolgingsplaner(ARBEIDSTAKER_FNR);
    }

    @Test(expected = RuntimeException.class)
    public void getOppfolgingsplanerServerError() {
        mockSvarFraTilgangTilBrukerViaAzure(ARBEIDSTAKER_FNR, INTERNAL_SERVER_ERROR);

        oppfolgingsplanInternController.getOppfolgingsplaner(ARBEIDSTAKER_FNR);
    }
}
