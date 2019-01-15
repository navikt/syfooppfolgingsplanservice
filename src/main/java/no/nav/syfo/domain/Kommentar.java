package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class Kommentar {
    public Long id;
    public long tiltakId;
    public String tekst;
    public String sistEndretAvAktoerId;
    public LocalDateTime sistEndretDato;
    public String opprettetAvAktoerId;
    public LocalDateTime opprettetDato;
}
