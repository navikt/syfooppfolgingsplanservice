package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class Sporingsinformasjon implements Serializable {
    public String endretAv;
    public String endretKilde;
    public String endretKildereferanse;
    public String endretTidspunkt;
    public String opprettetAv;
    public String opprettetKilde;
    public String opprettetKildereferanse;
    public String opprettetTidspunkt;
}
