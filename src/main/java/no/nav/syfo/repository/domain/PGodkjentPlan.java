package no.nav.syfo.repository.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PGodkjentPlan {
    public Long id;
    public long oppfoelgingsdialogId;
    public String dokumentUuid;
    public String sakId;
    public String journalpostId;
    public String tildeltEnhet;
    public boolean deltMedNav;
    public boolean deltMedFastlege;
    public boolean tvungenGodkjenning;
    public LocalDateTime avbruttTidspunkt;
    public String avbruttAv;
    public LocalDateTime fom;
    public LocalDateTime tom;
    public LocalDateTime evalueres;
    public LocalDateTime deltMedNavTidspunkt;
    public LocalDateTime deltMedFastlegeTidspunkt;
    public LocalDateTime created;
}
