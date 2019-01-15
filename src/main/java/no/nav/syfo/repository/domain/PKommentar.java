package no.nav.syfo.repository.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class PKommentar {
    public Long id;
    public long tiltakId;
    public String tekst;
    public String sistEndretAvAktoerId;
    public LocalDateTime sistEndretDato;
    public String opprettetAvAktoerId;
    public LocalDateTime opprettetDato;
}
