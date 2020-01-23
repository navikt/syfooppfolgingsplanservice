package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class AntallTimerForTimeloennet {
    public double antallTimer;
    public Periode periode;
    public String rapporteringsperiode;
    public Sporingsinformasjon sporingsinformasjon;
}
