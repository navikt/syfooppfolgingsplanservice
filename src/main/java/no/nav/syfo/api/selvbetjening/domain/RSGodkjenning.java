package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class RSGodkjenning {

    public boolean godkjent;
    public RSPerson godkjentAv;
    public String beskrivelse;
    public LocalDateTime godkjenningsTidspunkt;
    public RSGyldighetstidspunkt gyldighetstidspunkt;
}
