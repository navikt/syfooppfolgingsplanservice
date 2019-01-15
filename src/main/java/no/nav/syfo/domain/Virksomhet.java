package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Virksomhet {
    public String navn;
    public String virksomhetsnummer;
}
