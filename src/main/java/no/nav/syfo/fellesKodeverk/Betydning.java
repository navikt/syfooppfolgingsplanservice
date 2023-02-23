package no.nav.syfo.fellesKodeverk;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(fluent = true)
public class Betydning implements Serializable {
    public Map<String, Beskrivelse> beskrivelser;
    public String gyldigFra;
    public String gyldigTil;
}
