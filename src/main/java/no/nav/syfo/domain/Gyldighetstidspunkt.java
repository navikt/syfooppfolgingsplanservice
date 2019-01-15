package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(fluent = true)
public class Gyldighetstidspunkt {
    public LocalDate fom;
    public LocalDate tom;
    public LocalDate evalueres;
}
