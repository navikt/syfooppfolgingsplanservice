package no.nav.syfo.api.intern;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.status;
import static no.nav.metrics.MetricsFactory.createEvent;

@Slf4j
@Provider
public class RuntimeLogger implements ExceptionMapper<RuntimeException> {

    private static final String NO_BIGIP_5XX_REDIRECT = "X-Escape-5xx-Redirect";

    @Override
    public Response toResponse(RuntimeException e) {
        if (statuskode(e) == 500) {
            createEvent("runtimeexception").report();
            log.error("Runtimefeil ", e);
        }
        return status(statuskode(e)).header(NO_BIGIP_5XX_REDIRECT, true).build();
    }

    private int statuskode(RuntimeException e) {
        if (e instanceof WebApplicationException) {
            return ((WebApplicationException) e).getResponse().getStatus();
        } else {
            return 500;
        }
    }
}
