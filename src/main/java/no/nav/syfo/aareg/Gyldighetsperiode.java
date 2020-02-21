package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class Gyldighetsperiode implements Serializable {
    public String fom;
    public String tom;
}
