package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class Ansatt implements Serializable {
    public String aktoerId;
    public String virksomhetsnummer;
}
