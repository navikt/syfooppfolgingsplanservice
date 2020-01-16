package no.nav.syfo.aktorregister;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Identinfo {
    public String ident;
    public String identgruppe;
    public boolean gjeldende;
}
