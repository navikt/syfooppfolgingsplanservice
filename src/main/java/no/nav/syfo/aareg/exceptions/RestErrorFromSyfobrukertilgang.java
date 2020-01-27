package no.nav.syfo.aareg.exceptions;

import org.springframework.web.client.RestClientException;

public class RestErrorFromSyfobrukertilgang extends RuntimeException {
    public RestErrorFromSyfobrukertilgang(String message, RestClientException e) {
        super(message, e);
    }
}
