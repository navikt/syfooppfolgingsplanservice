package no.nav.syfo.service.exceptions;

import lombok.ToString;

@ToString
public class FeilDTO {
    public String id;
    public String type;
    public Detaljer detaljer;

    public FeilDTO() {
    }

    public FeilDTO(String id, String type, Detaljer detaljer) {
        this.id = id;
        this.type = type;
        this.detaljer = detaljer;
    }

    @ToString
    public static class Detaljer {
        public String detaljertType;
        public String feilMelding;
        public String stackTrace;

        public Detaljer() {
        }

        public Detaljer(String detaljertType, String feilMelding, String stackTrace) {
            this.detaljertType = detaljertType;
            this.feilMelding = feilMelding;
            this.stackTrace = stackTrace;
        }
    }

}
