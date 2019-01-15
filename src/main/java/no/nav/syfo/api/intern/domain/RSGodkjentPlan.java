package no.nav.syfo.api.intern.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class RSGodkjentPlan {
    public LocalDateTime opprettetTidspunkt;
    public RSGyldighetstidspunkt gyldighetstidspunkt;
    public boolean tvungenGodkjenning;
    public LocalDateTime deltMedNAVTidspunkt;
    public boolean deltMedNAV;
    public LocalDateTime deltMedFastlegeTidspunkt;
    public boolean deltMedFastlege;
    public String dokumentUuid;
}
