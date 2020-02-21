package no.nav.syfo.pdl;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.util.List;
import static no.nav.syfo.util.StringUtil.lowerCapitalize;

@Data
@Accessors(fluent = true)
public class PdlHentPerson implements Serializable {
    public PdlPerson hentPerson;

    public String getName() {
        if (hentPerson == null) {
            return null;
        }
        List<PdlPersonNavn> nameList = hentPerson.navn;
        if (nameList == null || nameList.isEmpty()) {
            return null;
        }

        PdlPersonNavn personNavn = nameList.get(0);
        String firstName = lowerCapitalize(personNavn.fornavn);
        String middleName = personNavn.mellomnavn;
        String surName = lowerCapitalize(personNavn.etternavn);

        if (middleName == null || middleName.isEmpty()) {
            return firstName + " " + surName;
        } else {
            return firstName + " " + lowerCapitalize(middleName) + " " + surName;
        }
    }
}
