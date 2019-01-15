package no.nav.syfo.repository.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PAsynkoppgave {
    public Long id;
    public LocalDateTime opprettetTidspunkt;
    public String oppgavetype;
    public Long avhengigAv;
    public int antallForsoek;
    public String ressursId;
}
