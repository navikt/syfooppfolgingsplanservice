package no.nav.syfo.pdl.exceptions;

public class PDLResponseBodyContainsError extends RuntimeException{
    public PDLResponseBodyContainsError(String message) {
        super(message);
    }
}
