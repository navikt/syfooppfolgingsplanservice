package no.nav.syfo.aktorregister.exceptions;

public class NoResponseForDesiredUser extends RuntimeException{
    public NoResponseForDesiredUser(String message) {
        super(message);
    }
}
