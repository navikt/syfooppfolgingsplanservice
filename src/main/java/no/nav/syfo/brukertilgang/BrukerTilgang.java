package no.nav.syfo.brukertilgang;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class BrukerTilgang {
    public boolean tilgang;
}
