package no.nav.syfo.repository.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PTiltak {
    public Long id;
    public long oppfoelgingsdialogId;
    public String navn;
    public LocalDateTime fom;
    public LocalDateTime tom;
    public String beskrivelse;
    public String opprettetAvAktoerId;
    public String sistEndretAvAktoerId;
    public LocalDateTime sistEndretDato;
    public LocalDateTime opprettetDato;
    public String status;
    public String gjennomfoering;
    public String beskrivelseIkkeAktuelt;
}
