package no.nav.syfo.sykmeldinger.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArbeidsgiverStatusDTO {
    public String orgnummer;
    public String juridiskOrgnummer;
    public String orgNavn;
}
