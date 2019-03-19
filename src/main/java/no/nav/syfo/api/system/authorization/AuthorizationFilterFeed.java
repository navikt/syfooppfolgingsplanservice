package no.nav.syfo.api.system.authorization;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;

public class AuthorizationFilterFeed implements Filter {

    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    class AutoriseringsFilterException extends RuntimeException {
        private AutoriseringsFilterException(String message) {
            super(message);
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (!erRequestAutorisert(httpServletRequest, basicCredentials())) {
            throw new AutoriseringsFilterException("Access denied");
        }
        chain.doFilter(request, response);
    }

    public void destroy() {
    }

    private static boolean erRequestAutorisert(HttpServletRequest httpServletRequest, String credential) {
        return ofNullable(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).map(credential::equals).orElse(false);
    }

    private static String basicCredentials() {
        return "Basic " + Base64.getEncoder().encodeToString(format("%s:%s", getenv("SYFOVEILEDEROPPGAVER_SYSTEMAPI_USERNAME"), getenv("SYFOVEILEDEROPPGAVER_SYSTEMAPI_PASSWORD")).getBytes());
    }
}
