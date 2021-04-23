package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Accessors(fluent = true)
public class Sykmeldingsperiode implements Serializable {
    public LocalDate fom;
    public LocalDate tom;
}
