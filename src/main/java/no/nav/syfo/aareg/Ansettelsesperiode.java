package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Ansettelsesperiode {
    public Bruksperiode bruksperiode;
    public Periode periode;
    public Sporingsinformasjon sporingsinformasjon;
    public String varslingskode;
}
