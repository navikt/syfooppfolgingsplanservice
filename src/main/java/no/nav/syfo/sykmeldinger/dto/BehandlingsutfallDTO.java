package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class BehandlingsutfallDTO {
    public RegelStatusDTO status;
    public List<RegelinfoDTO> ruleHits;
}
