package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(fluent = true)
public class BehandlingsutfallDTO implements Serializable {
    public RegelStatusDTO status;
    public List<RegelinfoDTO> ruleHits;
}
