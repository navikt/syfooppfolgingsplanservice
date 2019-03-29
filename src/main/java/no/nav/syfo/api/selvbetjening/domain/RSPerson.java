package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true, chain = true)
public class RSPerson {
    public String navn = " ";
    public String fnr;
    public String epost;
    public String tlf;
    public LocalDateTime sistInnlogget;
    public Boolean samtykke;
    public RSEvaluering evaluering;
    public List<RSStilling> stillinger = new ArrayList<>();
}
