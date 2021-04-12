package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.time.LocalDate;

@Data
@Accessors(fluent = true)
public class SykmeldingsperiodeDTO {
    public LocalDate fom;
    public LocalDate tom;
    public GradertDTO gradert;
    public int ehandlingsdager;
    public String innspillTilArbeidsgiver;
    public PeriodetypeDTO type;
    public AktivitetIkkeMuligDTO aktivitetIkkeMulig;
    public boolean reisetilskudd;
    
    public enum PeriodetypeDTO {
        AKTIVITET_IKKE_MULIG,
        AVVENTENDE,
        BEHANDLINGSDAGER,
        GRADERT,
        REISETILSKUDD
    }


}
