package no.nav.syfo.oppgave;

import no.nav.syfo.oppgave.Jobb;
import no.nav.syfo.oppgave.Oppgavetype;
import org.junit.Test;

import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;
import static org.assertj.core.api.Assertions.assertThat;

public class JobbTest {
    @Test(expected = RuntimeException.class)
    public void oppgavetypeManglerOverride() throws Exception {
        Jobb jobb = oppgavetype -> {
        };
        jobb.skalUtforeOppgave(OPPFOELGINGSDIALOG_SEND);
    }

    @Test
    public void oppgavetypeOverrideSkalUtforeOppgaveTrue() throws Exception {
        Jobb jobb = new Jobb() {
            @Override
            public void utfoerOppgave(String id) {
            }

            @Override
            public Oppgavetype oppgavetype() {
                return OPPFOELGINGSDIALOG_SEND;
            }
        };
        assertThat(jobb.skalUtforeOppgave(OPPFOELGINGSDIALOG_SEND)).isTrue();
    }

    @Test
    public void oppgavetyperOverrideSkalUtforeOppgaveFalse() throws Exception {
        Jobb jobb = new Jobb() {
            @Override
            public void utfoerOppgave(String id) {
            }

            @Override
            public Stream<Oppgavetype> oppgavetyper() {
                return of(OPPFOELGINGSDIALOG_ARKIVER);
            }
        };
        assertThat(jobb.skalUtforeOppgave(OPPFOELGINGSDIALOG_SEND)).isFalse();
    }
}
