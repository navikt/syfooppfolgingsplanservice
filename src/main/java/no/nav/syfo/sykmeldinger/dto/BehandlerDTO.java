package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class BehandlerDTO {
    public String fornavn;
    public String mellomnavn;
    public String etternavn;
    public String aktoerId;
    public String fnr;
    public String hpr;
    public String her;
    public AdresseDTO adresse;
    public String tlf;
}
