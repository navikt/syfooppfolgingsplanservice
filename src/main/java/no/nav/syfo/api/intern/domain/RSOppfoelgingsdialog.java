package no.nav.syfo.api.intern.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public List<RSVeilederOppgave> oppgaver = new ArrayList<>();

    public RSPerson arbeidsgiver;
    public RSPerson arbeidstaker;
}
