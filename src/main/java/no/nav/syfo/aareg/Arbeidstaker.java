package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Arbeidstaker {
    public String offentligIdent;
    public String aktoerId;
    public String type;
}
