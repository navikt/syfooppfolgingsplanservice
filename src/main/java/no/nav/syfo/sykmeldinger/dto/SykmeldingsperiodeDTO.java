package no.nav.syfo.sykmeldinger.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Data
@Accessors(fluent = true)
public class SykmeldingsperiodeDTO implements Serializable {
    public String fom;
    public String tom;
}
