package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class OrganisasjonInformasjon {
    public String orgnummer;
    public String juridiskOrgnummer;
    public String orgNavn;

    public OrganisasjonInformasjon(String orgnummer, String juridiskOrgnummer, String orgNavn) {
        this.orgnummer = orgnummer;
        this.juridiskOrgnummer = juridiskOrgnummer;
        this.orgNavn = orgNavn;
    }
}
