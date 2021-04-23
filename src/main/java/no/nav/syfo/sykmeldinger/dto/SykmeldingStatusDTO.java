package no.nav.syfo.sykmeldinger.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Accessors(fluent = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SykmeldingStatusDTO implements Serializable {
    public String statusEvent;
    public OffsetDateTime timestamp;
    public ArbeidsgiverStatusDTO arbeidsgiver;
}
