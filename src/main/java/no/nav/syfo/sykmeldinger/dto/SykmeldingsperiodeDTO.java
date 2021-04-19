package no.nav.syfo.sykmeldinger.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.time.LocalDate;

@Data
@Accessors(fluent = true)
public class SykmeldingsperiodeDTO {
    public LocalDate fom;
    public LocalDate tom;
}
