package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PrognoseDTO {
    public boolean arbeidsforEtterPeriode;
    public String hensynArbeidsplassen;
    public ErIArbeidDTO erIArbeid;
    public ErIkkeIArbeidDTO erIkkeIArbeid;
}
