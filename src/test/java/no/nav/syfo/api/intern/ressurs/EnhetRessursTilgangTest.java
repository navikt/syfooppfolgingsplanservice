package no.nav.syfo.api.intern.ressurs;

import no.nav.syfo.service.VeilederBehandlingService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeileder;
import static no.nav.syfo.testhelper.UserConstants.NAV_ENHET;
import static no.nav.syfo.testhelper.UserConstants.VEILEDER_ID;
import static org.springframework.http.HttpStatus.*;

public class EnhetRessursTilgangTest extends AbstractRessursTilgangTest {

    @MockBean
    VeilederBehandlingService veilederBehandlingService;

    @Inject
    private EnhetRessurs enhetRessurs;

    @Before
    public void setup() {
        loggInnVeileder(oidcRequestContextHolder, VEILEDER_ID);
    }

    @Test
    public void hentSykmeldte_har_tilgang() {
        mockSvarFraTilgangTilEnhet(NAV_ENHET, OK);

        enhetRessurs.hentSykmeldteMedUlesteOppfolgingsdialogerPaaEnhet(NAV_ENHET);
    }

    @Test(expected = ForbiddenException.class)
    public void hentSykmeldte_har_ikke_tilgang() {
        mockSvarFraTilgangTilEnhet(NAV_ENHET, FORBIDDEN);

        enhetRessurs.hentSykmeldteMedUlesteOppfolgingsdialogerPaaEnhet(NAV_ENHET);
    }

    @Test(expected = RuntimeException.class)
    public void hentDialoger_annen_tilgangsfeil() {
        mockSvarFraTilgangTilEnhet(NAV_ENHET, INTERNAL_SERVER_ERROR);

        enhetRessurs.hentSykmeldteMedUlesteOppfolgingsdialogerPaaEnhet(NAV_ENHET);
    }
}
