package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class LoggMelding {
    public String meldingsId;
    public String avsender;
    public String mottaker;
    public String joarkRef;
    public String meldingsInnhold;
    public Integer antallAarLagres;
}
