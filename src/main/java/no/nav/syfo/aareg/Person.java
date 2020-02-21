package no.nav.syfo.aareg;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class Person implements Serializable {
    public Type type;
    public String aktoerId;
    public String offentligIdent;

    public enum Type {
        Person
    }
}
