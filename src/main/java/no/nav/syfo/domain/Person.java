package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class Person {
    public String navn;
    public String aktoerId;
    public String fnr;
    public String epost;
    public String tlf;
    public LocalDateTime sistInnlogget;
    public LocalDateTime sisteEndring;
    public LocalDateTime sistAksessert;
    public Boolean samtykke;
}
