package no.nav.syfo.api.system;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;

import static java.lang.System.setProperty;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthorizationFilter authorizationFilter;

    @Before
    public void setup() {
        setProperty("syfoveilederoppgaver.systemapi.username", "username");
        setProperty("syfoveilederoppgaver.systemapi.password", "password");
    }

    @Test(expected = NotAuthorizedException.class)
    public void kaster401dersomIkkeriktigAuthorizationHeader() throws IOException, ServletException {
        when(request.getHeader(AUTHORIZATION)).thenReturn("Basic feilverdi");

        authorizationFilter.doFilter(request, response, filterChain);
    }

    @Test(expected = NotAuthorizedException.class)
    public void kaster401dersomIkkeIngenAuthorizationHeader() throws IOException, ServletException {
        when(request.getHeader(AUTHORIZATION)).thenReturn(null);

        authorizationFilter.doFilter(request, response, filterChain);
    }

    @Test
    public void tilgangOmRiktigHeaderVerdi() throws IOException, ServletException {
        when(request.getHeader(AUTHORIZATION)).thenReturn("Basic bnVsbDpudWxs");

        authorizationFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(any(), any());
    }
}

