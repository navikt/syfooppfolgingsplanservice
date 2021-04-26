package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Accessors(fluent = true)
public class SykmeldingStatusDTO implements Serializable {
    public String statusEvent;
    public OffsetDateTime timestamp;
    public ArbeidsgiverStatusDTO arbeidsgiver;
}
