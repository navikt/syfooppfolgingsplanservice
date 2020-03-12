package no.nav.syfo.api.intern.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class RSOppfoelgingsdialog {
    public Long id;
    public String uuid;

    public String sistEndretAvAktoerId;
    public LocalDateTime sistEndretDato;

    public String status;
    public RSVirksomhet virksomhet;

    public RSGodkjentPlan godkjentPlan = null;

    public RSPerson arbeidsgiver;
    public RSPerson arbeidstaker;
}
