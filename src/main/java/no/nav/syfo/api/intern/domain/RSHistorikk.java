package no.nav.syfo.api.intern.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class RSHistorikk {
    public String opprettetAv;
    public String tekst;
    public LocalDateTime tidspunkt;
}
