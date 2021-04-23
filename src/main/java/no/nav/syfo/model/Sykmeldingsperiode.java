package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class Sykmeldingsperiode {
    public LocalDateTime fom;
    public LocalDateTime tom;
}
