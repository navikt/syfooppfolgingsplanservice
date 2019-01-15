package no.nav.syfo.util;

import javax.ws.rs.WebApplicationException;

public class ConflictException extends WebApplicationException {

    public ConflictException() {
        super(409); // 409
    }
}
