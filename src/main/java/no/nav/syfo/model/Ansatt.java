package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Ansatt {
    public String aktoerId;
    public String virksomhetsnummer;
}
