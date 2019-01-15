package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class Tiltak {
    public Long id;
    public long oppfoelgingsdialogId;
    public String navn;
    public LocalDate fom;
    public LocalDate tom;
    public String beskrivelse;
    public String status;
    public String gjennomfoering;
    public String beskrivelseIkkeAktuelt;
    public List<Kommentar> kommentarer = new ArrayList<>();

    public LocalDateTime opprettetDato;
    public LocalDateTime sistEndretDato;

    public String opprettetAvAktoerId;
    public String sistEndretAvAktoerId;

}

