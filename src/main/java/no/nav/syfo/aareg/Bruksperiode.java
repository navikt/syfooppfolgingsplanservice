package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Bruksperiode {
    public String fom;
    public String tom;
}
