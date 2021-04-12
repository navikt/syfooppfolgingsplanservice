package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.time.LocalDate;

import java.util.List;

@Data
@Accessors(fluent = true)
public class MedisinskVurderingDTO {
    public DiagnoseDTO hovedDiagnose;
    public List<DiagnoseDTO> biDiagnoser;
    public AnnenFraversArsakDTO annenFraversArsak;
    public boolean svangerskap;
    public boolean yrkesskade;
    public LocalDate yrkesskadeDato;
}
