package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Kontaktinfo {
    public String epost;
    public String tlf;
    public Boolean skalHaVarsel;
    public FeilAarsak feilAarsak;

    public enum FeilAarsak {
        RESERVERT,
        UTGAATT,
        KONTAKTINFO_IKKE_FUNNET,
        SIKKERHETSBEGRENSNING,
        PERSON_IKKE_FUNNET
    }
}

