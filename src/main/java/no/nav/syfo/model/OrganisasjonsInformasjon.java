package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class OrganisasjonsInformasjon {
    public String orgnummer;
    public String orgNavn;
}
