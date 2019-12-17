package no.nav.syfo.pdl;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@JsonSerialize
public class PdlRequest {
    public String query;
    public Variables variables;
}
