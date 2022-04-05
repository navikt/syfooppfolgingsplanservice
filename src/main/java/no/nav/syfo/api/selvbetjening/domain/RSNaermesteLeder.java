package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class RSNaermesteLeder extends RSPerson {
    public String virksomhetsnummer;
    public boolean erAktiv;
    public LocalDate aktivFom;
    public LocalDate aktivTom;
    public String navn = " ";
    public String fnr;
    public String epost;
    public String tlf;
    public LocalDateTime sistInnlogget;
    public Boolean samtykke;
    public RSEvaluering evaluering;
}
