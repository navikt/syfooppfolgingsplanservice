package no.nav.syfo.api.intern.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSBrukerPaaEnhet {
    public String fnr;
    public boolean skjermetEllerEgenAnsatt;
}
