package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ArbeidsgiverStatusDTO {
    public String orgnummer;
    public String juridiskOrgnummer;
    public String orgNavn;
}
