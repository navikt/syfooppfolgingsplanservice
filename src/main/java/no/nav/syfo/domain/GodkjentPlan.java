package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Optional.empty;

@Data
@Accessors(fluent = true)
public class GodkjentPlan {
    public long id;
    public long oppfoelgingsdialogId;
    public LocalDateTime opprettetTidspunkt;
    public Gyldighetstidspunkt gyldighetstidspunkt;
    public boolean tvungenGodkjenning;
    public LocalDateTime deltMedNAVTidspunkt;
    public boolean deltMedNAV;
    public boolean deltMedFastlege;
    public LocalDateTime deltMedFastlegeTidspunkt;
    public String dokumentUuid;
    public Optional<Avbruttplan> avbruttPlan = empty();
    public String sakId;
    public String journalpostId;
    public String tildeltEnhet;
}
