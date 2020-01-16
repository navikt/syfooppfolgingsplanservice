package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PermisjonPermittering {
    public Periode periode;
    public String permisjonPermitteringId;
    public double prosent;
    public Sporingsinformasjon sporingsinformasjon;
    public String type;
}
