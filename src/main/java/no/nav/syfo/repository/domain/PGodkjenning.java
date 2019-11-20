package no.nav.syfo.repository.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PGodkjenning {
    public Long id;
    public long oppfoelgingsdialogId;
    public LocalDateTime created;
    public String aktoerId;
    public String beskrivelse;
    public boolean godkjent;
    public boolean delMedNav;
    public LocalDateTime fom;
    public LocalDateTime tom;
    public LocalDateTime evalueres;
}
