package no.nav.syfo.api.intern.ressurs;

import no.nav.brukerdialog.security.context.SubjectRule;
import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.syfo.service.TilgangsKontroll;
import org.jose4j.jwt.ReservedClaimNames;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.common.auth.SsoToken.oidcToken;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Hensikten her er å samle koden som mock svar fra syfo-tilgangskontroll.
 * Subklasser arver tilgangskontrollResponse, som de kan sette opp til å returnere 200 OK, 403 Forbidden eller
 * 500-feil.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientBuilder.class)
public abstract class AbstractRessursTilgangTest {

    private static final int EXPIRATION_TIME = Integer.MAX_VALUE;

    static final String FNR = "123456789";

    private static Client client;
    private static TilgangsKontroll tilgangService;
    private static final String EXPIRATION_TIME_ATTRIBUTE_NAME = ReservedClaimNames.EXPIRATION_TIME;

    @Mock
    Response tilgangskontrollResponse;

    @Rule
    public SubjectRule subjectRule = new SubjectRule();

    @BeforeClass
    public static void initialize() {
        mockStatic(ClientBuilder.class);
        client = mock(Client.class);
        when(ClientBuilder.newClient()).thenReturn(client);
        tilgangService = spy(new TilgangsKontroll());
    }

    @Before
    public void setUp() {
        // Mock REST-klienten
        Invocation.Builder builderMock = mock(Invocation.Builder.class);
        when(builderMock.get()).thenReturn(tilgangskontrollResponse);
        when(builderMock.header(anyString(), anyString())).thenReturn(builderMock);

        final WebTarget webTargetMock = mock(WebTarget.class);
        when(webTargetMock.request(APPLICATION_JSON)).thenReturn(builderMock);
        when(webTargetMock.queryParam(anyString(), anyString())).thenReturn(webTargetMock);

        when(client.target(anyString())).thenReturn(webTargetMock);

        gittBrukerMedOidcAssertation();
    }

    private void gittBrukerMedOidcAssertation() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(EXPIRATION_TIME_ATTRIBUTE_NAME, EXPIRATION_TIME);
        SsoToken oidcToken = oidcToken("token", attributes);

        Subject subject = new Subject(FNR, IdentType.InternBruker, oidcToken);
        subjectRule.setSubject(subject);
    }
}
