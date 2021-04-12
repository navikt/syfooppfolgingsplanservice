package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class ArbeidsrelatertArsakDTO {
    public String beskrivelse;
    List<ArbeidsrelatertArsakTypeDTO> arsak;

    public enum ArbeidsrelatertArsakTypeDTO {
        MANGLENDE_TILRETTELEGGING("1", "Manglende tilrettelegging på arbeidsplassen"),
        ANNET("9", "Annet");

        private String codeValue;
        private String text;
        private final String oid = "2.16.578.1.12.4.1.1.8132";

        ArbeidsrelatertArsakTypeDTO(String codeValue, String text) {
            this.codeValue = codeValue;
            this.text = text;
        }
    }
}
