package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class Utenlandsopphold implements Serializable {
    public String landkode;
    public Periode periode;
    public String rapporteringsperiode;
    public Sporingsinformasjon sporingsinformasjon;
}
