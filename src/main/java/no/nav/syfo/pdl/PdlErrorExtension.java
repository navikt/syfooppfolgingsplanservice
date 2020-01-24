package no.nav.syfo.pdl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PdlErrorExtension {
    public String code;
    public String classification;
}