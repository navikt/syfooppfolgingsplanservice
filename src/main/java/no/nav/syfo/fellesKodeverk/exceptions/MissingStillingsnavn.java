package no.nav.syfo.fellesKodeverk.exceptions;

public class MissingStillingsnavn extends RuntimeException {
    public MissingStillingsnavn(String message, Exception e) {
        super(message, e);
    }
}
