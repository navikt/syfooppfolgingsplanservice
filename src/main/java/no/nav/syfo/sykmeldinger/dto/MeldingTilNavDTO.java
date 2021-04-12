package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class MeldingTilNavDTO {
    public boolean bistandUmiddelbart;
    public String beskrivBistand;
}
