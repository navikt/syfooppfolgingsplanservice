package no.nav.syfo.api.intern.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class RSPerson {
    public String navn;
    public String aktoerId;
    public String fnr;
    public String epost;
    public String tlf;
    public LocalDateTime sistInnlogget;
    public Boolean samtykke;
}
