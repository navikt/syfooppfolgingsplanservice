package no.nav.syfo.sykmeldinger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Data
@Accessors(fluent = true)
@JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
public class SykmeldingsperiodeDTO implements Serializable {
    public String fom;
    public String tom;
}
