package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class OpplysningspliktigArbeidsgiver {
    public String organisasjonsnummer;
    public Type type;

    public enum Type {
        Organisasjon,
        Person
    }
}
