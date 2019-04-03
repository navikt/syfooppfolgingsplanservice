package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSVirksomhet {
    public String virksomhetsnummer;
    public String navn = "";
}
