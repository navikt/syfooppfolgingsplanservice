package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = true)
public class RSEvaluering {
    public String effekt;
    public String hvorfor;
    public String videre;
    public boolean interneaktiviteter;
    public boolean ekstratid;
    public boolean bistand;
    public boolean ingen;
}
