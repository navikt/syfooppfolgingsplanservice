package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ArbeidsgiverDTO {
    public String navn;
    public int stillingsprosent;
}
