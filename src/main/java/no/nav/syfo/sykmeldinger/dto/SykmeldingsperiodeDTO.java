package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Data
@Accessors(fluent = true)
public class SykmeldingsperiodeDTO {
    public LocalDateTime fom;
    public LocalDateTime tom;
}
