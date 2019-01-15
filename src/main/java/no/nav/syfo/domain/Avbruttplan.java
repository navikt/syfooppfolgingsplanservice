package no.nav.syfo.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class Avbruttplan {
    public String avAktoerId;
    public LocalDateTime tidspunkt;
    public Long oppfoelgingsdialogId;
}
