package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class Godkjenning {
    public long id;
    public long oppfoelgingsdialogId;

    public boolean godkjent;
    public boolean delMedNav;
    public String godkjentAvAktoerId;
    public String beskrivelse;
    public LocalDateTime godkjenningsTidspunkt;
    public Gyldighetstidspunkt gyldighetstidspunkt = new Gyldighetstidspunkt();
}
