package no.nav.syfo.api.intern.ressurs;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.oidc.OIDCIssuer;
import no.nav.syfo.service.VeilederTilgangService;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

import static no.nav.syfo.service.VeilederTilgangService.TILGANG_TIL_BRUKER_VIA_AZURE_PATH;
import static no.nav.syfo.service.VeilederTilgangService.TILGANG_TIL_TJENESTEN_VIA_AZURE_PATH;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

/**
 * Hensikten her er å samle koden som mock svar fra syfo-tilgangskontroll.
 * Subklasser arver tilgangskontrollResponse, som de kan sette opp til å returnere 200 OK, 403 Forbidden eller
 * 500-feil.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public abstract class AbstractRessursTilgangTest {

    @Value("${tilgangskontrollapi.url}")
    private String tilgangskontrollUrl;

    @Value("${dev}")
    private String dev;

    @Inject
    public OIDCRequestContextHolder oidcRequestContextHolder;

    @Inject
    private RestTemplate restTemplate;

    private MockRestServiceServer mockRestServiceServer;

    @Before
    public void setUp() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @After
    public void tearDown() {
        mockRestServiceServer.verify();
        loggUtAlle(oidcRequestContextHolder);
    }

    public void mockSvarFraTilgangTilBrukerViaAzure(String fnr, HttpStatus status) {
        String uriString = fromHttpUrl(tilgangskontrollUrl)
                .path(TILGANG_TIL_BRUKER_VIA_AZURE_PATH)
                .queryParam(VeilederTilgangService.FNR, fnr)
                .toUriString();

        String idToken = oidcRequestContextHolder.getOIDCValidationContext().getToken(OIDCIssuer.AZURE).getIdToken();

        mockRestServiceServer.expect(manyTimes(), requestTo(uriString))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, "Bearer " + idToken))
                .andRespond(withStatus(status));
    }

    public void mockSvarFraTilgangTilTjenestenViaAzure(HttpStatus status) {
        String uriString = fromHttpUrl(tilgangskontrollUrl)
                .path(TILGANG_TIL_TJENESTEN_VIA_AZURE_PATH)
                .toUriString();

        String idToken = oidcRequestContextHolder.getOIDCValidationContext().getToken(OIDCIssuer.AZURE).getIdToken();

        mockRestServiceServer.expect(manyTimes(), requestTo(uriString))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, "Bearer " + idToken))
                .andRespond(withStatus(status));
    }
}
