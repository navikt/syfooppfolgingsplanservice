package no.nav.syfo.pdl;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class PdlPersonNavn implements Serializable {
    public String fornavn;
    public String mellomnavn;
    public String etternavn;
}
