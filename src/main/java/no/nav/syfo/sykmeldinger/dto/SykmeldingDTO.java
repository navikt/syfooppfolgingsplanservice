package no.nav.syfo.sykmeldinger.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SykmeldingDTO {
    public String id;
    public BehandlingsutfallDTO behandlingsutfall;
    public List<SykmeldingsperiodeDTO> sykmeldingsperioder;
    public SykmeldingStatusDTO sykmeldingStatus;
}


