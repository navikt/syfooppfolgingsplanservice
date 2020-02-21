package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class PermisjonPermittering implements Serializable {
    public Periode periode;
    public String permisjonPermitteringId;
    public double prosent;
    public Sporingsinformasjon sporingsinformasjon;
    public String type;
}
