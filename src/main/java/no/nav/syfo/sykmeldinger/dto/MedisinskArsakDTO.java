package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class MedisinskArsakDTO {
    public String beskrivelse;
    public List<MedisinskArsakTypeDTO> arsak;

    public enum MedisinskArsakTypeDTO {
        TILSTAND_HINDRER_AKTIVITET("1", "Helsetilstanden hindrer pasienten i å være i aktivitet"),
        AKTIVITET_FORVERRER_TILSTAND("2", "Aktivitet vil forverre helsetilstanden"),
        AKTIVITET_FORHINDRER_BEDRING("3", "Aktivitet vil hindre/forsinke bedring av helsetilstanden"),
        ANNET("9", "Annet");

        private String codeValue;
        private String text;
        private final String oid = "2.16.578.1.12.4.1.1.8133";

        MedisinskArsakTypeDTO(String codeValue, String text) {
            this.codeValue = codeValue;
            this.text = text;
        }
    }
}
