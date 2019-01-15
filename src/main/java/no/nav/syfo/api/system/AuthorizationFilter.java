package no.nav.syfo.api.system;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;
import java.util.Base64;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.syfo.util.PropertyUtil.SYFOVEILEDEROPPGAVER_SYSTEMAPI;

public class AuthorizationFilter implements Filter {
    public static final String BASIC_CREDENTIALS = basicCredentials(SYFOVEILEDEROPPGAVER_SYSTEMAPI);

    public void init(FilterConfig filterConfig) throws ServletException { }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (!erRequestAutorisert(httpServletRequest)) {
            throw new NotAuthorizedException("Access denied");
        }
        chain.doFilter(request, response);
    }

    private boolean erRequestAutorisert(HttpServletRequest httpServletRequest) {
        return ofNullable(httpServletRequest.getHeader(AUTHORIZATION)).map(BASIC_CREDENTIALS::equals).orElse(false);
    }

    public void destroy() { }

    public static String basicCredentials(String credential) {
        return "Basic " + Base64.getEncoder().encodeToString(format("%s:%s", getProperty(credential + "_USERNAME"), getProperty(credential + "_PASSWORD")).getBytes());
    }
}
