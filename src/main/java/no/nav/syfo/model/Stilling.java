package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(fluent = true)
public class Stilling {
    public String yrke;
    public BigDecimal prosent;
    public LocalDate fom;
    public LocalDate tom;
    public String orgnummer;
}
