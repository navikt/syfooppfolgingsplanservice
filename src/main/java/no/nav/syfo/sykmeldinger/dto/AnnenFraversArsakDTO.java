package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class AnnenFraversArsakDTO {
    public String beskrivelse;
    public List<AnnenFraverGrunnDTO> grunn;

    public enum AnnenFraverGrunnDTO {
        GODKJENT_HELSEINSTITUSJON,
        BEHANDLING_FORHINDRER_ARBEID,
        ARBEIDSRETTET_TILTAK,
        MOTTAR_TILSKUDD_GRUNNET_HELSETILSTAND,
        NODVENDIG_KONTROLLUNDENRSOKELSE,
        SMITTEFARE,
        ABORT,
        UFOR_GRUNNET_BARNLOSHET,
        DONOR,
        BEHANDLING_STERILISERING
    }
}
