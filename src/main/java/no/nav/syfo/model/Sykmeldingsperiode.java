package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.time.LocalDate;

@Data
@Accessors(fluent = true)
public class Sykmeldingsperiode {
    public LocalDate fom;
    public LocalDate tom;
}