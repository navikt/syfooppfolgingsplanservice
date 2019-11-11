package no.nav.syfo.domain;

import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Value(staticConstructor = "of")
public class Fnr {

    @NotEmpty
    @Pattern(regexp = "^[0-9]{11}$")
    String fnr;

    @Override
    public boolean equals(Object o) {
        return fnr.equals(o);
    }

    @Override
    public int hashCode() {
        return fnr.hashCode();
    }

    @Override
    public String toString() {
        return fnr;
    }

}
