package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Data
@Accessors(fluent = true)
public class SykmeldingsperiodeDTO {
    public String fom;
    public String tom;
}
