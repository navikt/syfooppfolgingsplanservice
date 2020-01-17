package no.nav.syfo.aktorregister;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class IdentinfoForAktoer {
    public List<Identinfo> identer;
    public String feilmelding;
}
