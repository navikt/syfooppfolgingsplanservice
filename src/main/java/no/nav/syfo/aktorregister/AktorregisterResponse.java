package no.nav.syfo.aktorregister;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(fluent = true)
public class AktorregisterResponse {
    public Map<String, IdentinfoForAktoer> aktorMap;
}
