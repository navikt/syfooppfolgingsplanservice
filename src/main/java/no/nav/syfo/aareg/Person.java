package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Person {
    public Type type;
    public String aktoerId;
    public String offentligIdent;

    public enum Type {
        Person
    }
}
