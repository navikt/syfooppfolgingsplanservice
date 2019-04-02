package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class RSArbeidsoppgave {
    public Long arbeidsoppgaveId;
    public String arbeidsoppgavenavn;

    public boolean erVurdertAvSykmeldt;
    public RSGjennomfoering gjennomfoering;
    public LocalDateTime opprettetDato;
    public LocalDateTime sistEndretDato;

    public RSPerson sistEndretAv;
    public RSPerson opprettetAv;
}
