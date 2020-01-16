package no.nav.syfo.aareg.exceptions;

import org.springframework.web.client.RestClientException;

public class RestErrorFromAareg extends RuntimeException {
    public RestErrorFromAareg(String message, RestClientException e) {
        super(message, e);
    }
}
