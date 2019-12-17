package no.nav.syfo.pdl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PdlPersonNavn {
    public String fornavn;
    public String mellomnavn;
    public String etternavn;
}
