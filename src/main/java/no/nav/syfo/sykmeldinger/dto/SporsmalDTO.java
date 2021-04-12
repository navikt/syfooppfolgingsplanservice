package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class SporsmalDTO {
    public String tekst;
    public ShortNameDTO shortName;
    public SvarDTO svar;

    public enum ShortNameDTO {
        ARBEIDSSITUASJON, NY_NARMESTE_LEDER, FRAVAER, PERIODE, FORSIKRING
    }
}
