package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(fluent = true)
public class Stilling {
    public String yrke;
    public BigDecimal prosent;
}
