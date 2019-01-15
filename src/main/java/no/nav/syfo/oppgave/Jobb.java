package no.nav.syfo.oppgave;

import java.util.stream.Stream;

import static java.util.stream.Stream.of;

public interface Jobb {
    void utfoerOppgave(String id);

    default Oppgavetype oppgavetype(){
        throw new RuntimeException("Må override enten oppgavetype eller oppgavetyper");
    }

    default Stream<Oppgavetype> oppgavetyper(){
        return of(oppgavetype());
    }

    default boolean skalUtforeOppgave(Oppgavetype oppgavetype) {
        return oppgavetyper().anyMatch(oppgavetype::equals);
    }
}
