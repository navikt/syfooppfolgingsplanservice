package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class OpplysningspliktigArbeidsgiver implements Serializable {
    public String organisasjonsnummer;
    public Type type;

    public enum Type {
        Organisasjon,
        Person
    }
}
