package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class ArbeidsgiverStatusDTO implements Serializable {
    public String orgnummer;
    public String juridiskOrgnummer;
    public String orgNavn;
}
