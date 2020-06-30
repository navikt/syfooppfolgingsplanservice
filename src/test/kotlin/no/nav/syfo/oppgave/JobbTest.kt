package no.nav.syfo.oppgave

import org.assertj.core.api.Assertions
import org.junit.Test
import java.util.stream.Stream

class JobbTest {
    @Test(expected = RuntimeException::class)
    fun oppgavetypeManglerOverride() {
        val jobb = Jobb { }
        jobb.skalUtforeOppgave(Oppgavetype.OPPFOELGINGSDIALOG_SEND)
    }

    @Test
    fun oppgavetypeOverrideSkalUtforeOppgaveTrue() {
        val jobb: Jobb = object : Jobb {
            override fun utfoerOppgave(id: String) {}
            override fun oppgavetype(): Oppgavetype {
                return Oppgavetype.OPPFOELGINGSDIALOG_SEND
            }
        }
        Assertions.assertThat(jobb.skalUtforeOppgave(Oppgavetype.OPPFOELGINGSDIALOG_SEND)).isTrue()
    }

    @Test
    fun oppgavetyperOverrideSkalUtforeOppgaveFalse() {
        val jobb: Jobb = object : Jobb {
            override fun utfoerOppgave(id: String) {}
            override fun oppgavetyper(): Stream<Oppgavetype> {
                return Stream.of(Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER)
            }
        }
        Assertions.assertThat(jobb.skalUtforeOppgave(Oppgavetype.OPPFOELGINGSDIALOG_SEND)).isFalse()
    }
}
