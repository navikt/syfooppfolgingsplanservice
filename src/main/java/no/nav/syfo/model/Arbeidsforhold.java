package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(fluent = true)
public class Arbeidsforhold {
    public String orgnummer;
    public List<Stilling> stillinger;
    public LocalDate fom;
    public LocalDate tom;
}
