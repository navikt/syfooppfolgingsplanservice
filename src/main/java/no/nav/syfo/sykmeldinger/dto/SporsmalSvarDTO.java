package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class SporsmalSvarDTO {
    public String sporsmal;
    public String svar;
    List<SvarRestriksjonDTO> restriksjoner;

    public enum SvarRestriksjonDTO {
        SKJERMET_FOR_ARBEIDSGIVER("A", "Informasjonen skal ikke vises arbeidsgiver"),
        SKJERMET_FOR_PASIENT("P", "Informasjonen skal ikke vises pasient"),
        SKJERMET_FOR_NAV("N", "Informasjonen skal ikke vises NAV");

        private String codeValue;
        private String text;
        private final String oid = "2.16.578.1.12.4.1.1.8134";

        SvarRestriksjonDTO(String codeValue, String text) {
            this.codeValue = codeValue;
            this.text = text;
        }
    }
}
