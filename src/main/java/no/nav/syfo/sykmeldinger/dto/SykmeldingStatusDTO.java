package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Accessors(fluent = true)
public class SykmeldingStatusDTO {
    public String statusEvent;
    public OffsetDateTime timestamp;
    public ArbeidsgiverStatusDTO arbeidsgiver;
    List<SporsmalDTO> sporsmalOgSvarListe;

}
