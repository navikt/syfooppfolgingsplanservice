package no.nav.syfo.fellesKodeverk;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class Beskrivelse implements Serializable {
    public String tekst;
    public String term;
}
