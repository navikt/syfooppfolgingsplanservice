package no.nav.syfo.fellesKodeverk;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(fluent = true)
public class KodeverkKoderBetydningerResponse {
    public Map<String, List<Betydning>> betydninger;
}
