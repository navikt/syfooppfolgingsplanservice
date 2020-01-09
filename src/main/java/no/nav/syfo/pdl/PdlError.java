package no.nav.syfo.pdl;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class PdlError {
    public String message;
    public List<PdlErrorLocation> locations;
    public List<String> path;
    public PdlErrorExtension extensions;

    public String toSimplifiedString() {
        return message + " with code: " + extensions.code + " and classification: " + extensions.classification;
    }
}
