package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class RSTiltak {
    public Long tiltakId;
    public String tiltaknavn;
    public Long knyttetTilArbeidsoppgaveId;
    public LocalDate fom;
    public LocalDate tom;
    public String beskrivelse;
    public String beskrivelseIkkeAktuelt;

    public LocalDateTime opprettetDato;
    public LocalDateTime sistEndretDato;

    public List<RSKommentar> kommentarer = new ArrayList<>();
    public String status;
    public String gjennomfoering;

    public RSPerson opprettetAv;
    public RSPerson sistEndretAv;
}
