package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Gyldighetsperiode {
    public String fom;
    public String tom;
}
