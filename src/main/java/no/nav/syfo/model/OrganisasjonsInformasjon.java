package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class OrganisasjonsInformasjon implements Serializable {
    public String orgnummer;
    public String orgNavn;
}
