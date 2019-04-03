package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(fluent = true)
public class RSGyldighetstidspunkt {
    public LocalDate fom;
    public LocalDate tom;
    public LocalDate evalueres;
}
