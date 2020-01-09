package no.nav.syfo.pdl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PdlErrorLocation {
    public int line;
    public int column;
}
