package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSTilgang {
    public boolean harTilgang;
    public String ikkeTilgangGrunn;
}
