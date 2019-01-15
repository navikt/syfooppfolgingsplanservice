package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.syfo.oppgave.Oppgavetype;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNull;

@Data
@Accessors(fluent = true)
public class AsynkOppgave {
    public Long id;
    public LocalDateTime opprettetTidspunkt = now();
    public String oppgavetype;
    public Long avhengigAv;
    public int antallForsoek;
    public String ressursId;

    public AsynkOppgave() {
    }

    public AsynkOppgave(Oppgavetype type, String ressursId) {
        this.oppgavetype = requireNonNull(type.name());
        this.ressursId = requireNonNull(ressursId);
    }

    public AsynkOppgave(Oppgavetype type, String ressursId, Long avhengigAv) {
        this(type, ressursId);
        this.avhengigAv = requireNonNull(avhengigAv);
    }

    public AsynkOppgave inkrementerAntallForsoek() {
        this.antallForsoek++;
        return this;
    }
}
