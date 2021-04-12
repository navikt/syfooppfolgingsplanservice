package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.time.LocalDate;

@Data
@Accessors(fluent = true)
public class ErIArbeidDTO {
    public boolean egetArbeidPaSikt;
    public boolean annetArbeidPaSikt;
    public LocalDate arbeidFOM;
    public LocalDate vurderingsdato;
}
