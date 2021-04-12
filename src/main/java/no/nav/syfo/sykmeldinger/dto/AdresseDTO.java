package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors
public class AdresseDTO {
    public String gate;
    public int postnummer;
    public String kommune;
    public String postboks;
    public String land;
}