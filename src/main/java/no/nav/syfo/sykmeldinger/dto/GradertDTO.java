package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class GradertDTO {
    public int grad;
    public boolean reisetilskudd;
}
