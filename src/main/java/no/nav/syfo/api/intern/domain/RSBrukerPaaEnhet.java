package no.nav.syfo.api.intern.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSBrukerPaaEnhet {
    public String fnr;
    public Skjermingskode skjermetEllerEgenAnsatt;

    public enum Skjermingskode {
        KODE_6,
        KODE_7,
        EGEN_ANSATT,
        INGEN
    }
}
