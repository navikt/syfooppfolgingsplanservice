package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class Ansettelsesperiode implements Serializable {
    public Bruksperiode bruksperiode;
    public Periode periode;
    public Sporingsinformasjon sporingsinformasjon;
    public String varslingskode;
}
