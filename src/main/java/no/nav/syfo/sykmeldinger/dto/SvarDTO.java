package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class SvarDTO {
    public SvarTypeDTO svarType;
    public String svar;

    public enum SvarTypeDTO {
        ARBEIDSSITUASJON, PERIODER, JA_NEI
    }
}
