package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.time.LocalDate;

@Data
@Accessors(fluent = true)
public class KontaktMedPasientDTO {
    public LocalDate kontaktDato;
    public String begrunnelseIkkeKontakt;
}
