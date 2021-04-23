package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(fluent = true)
public class SykmeldingsperiodeDTO {
    public LocalDate fom;
    public LocalDate tom;
}
