package no.nav.syfo.api.system.authorization;

import no.nav.security.oidc.api.Unprotected;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.System.setProperty;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class AuthorizationFilterFeedTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthorizationFilterFeed authorizationFilterFeed;

    @Before
    public void setup() {
        setProperty("syfoveilederoppgaver.systemapi.username", "username");
        setProperty("syfoveilederoppgaver.systemapi.password", "password");
    }

    @Unprotected
    @Test(expected = AuthorizationFilterFeed.AutoriseringsFilterException.class)
    public void kaster401dersomIkkeriktigAuthorizationHeader() throws IOException, ServletException {
        when(request.getHeader(AUTHORIZATION)).thenReturn("Basic feilverdi");

        authorizationFilterFeed.doFilter(request, response, filterChain);
    }

    @Test(expected = AuthorizationFilterFeed.AutoriseringsFilterException.class)
    public void kaster401dersomIkkeIngenAuthorizationHeader() throws IOException, ServletException {
        when(request.getHeader(AUTHORIZATION)).thenReturn(null);

        authorizationFilterFeed.doFilter(request, response, filterChain);
    }

    @Test
    public void tilgangOmRiktigHeaderVerdi() throws IOException, ServletException {
        when(request.getHeader(AUTHORIZATION)).thenReturn("Basic bnVsbDpudWxs");

        authorizationFilterFeed.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(any(), any());
    }
}

