package no.nav.syfo.pdl;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class PdlPersonResponse {
    public List<PdlError> errors;
    public PdlHentPerson data;
}
