package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RegelinfoDTO {
    public String messageForSender;
    public String messageForUser;
    public String ruleName;
    public BehandlingsutfallDTO.RegelStatusDTO ruleStatus;
}
