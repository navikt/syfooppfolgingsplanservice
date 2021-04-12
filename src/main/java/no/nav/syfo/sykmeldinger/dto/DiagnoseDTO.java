package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class DiagnoseDTO {
    public String kode;
    public String system;
    public String tekst;
}
