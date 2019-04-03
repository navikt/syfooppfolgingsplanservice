package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class RSAvbruttplan {

    public RSPerson av;
    public LocalDateTime tidspunkt;
    public Long id;
}
