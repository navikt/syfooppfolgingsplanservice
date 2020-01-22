package no.nav.syfo.aktorregister.exceptions;

public class NoCurrentIdentForAktor extends RuntimeException {
    public NoCurrentIdentForAktor(String message) {
        super(message);
    }
}
