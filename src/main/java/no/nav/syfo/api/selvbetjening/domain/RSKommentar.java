package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class RSKommentar {
    public Long id;
    public String tekst;
    public LocalDateTime opprettetTidspunkt;
    public LocalDateTime sistEndretDato;
    public RSPerson opprettetAv;
    public RSPerson sistEndretAv;
}
