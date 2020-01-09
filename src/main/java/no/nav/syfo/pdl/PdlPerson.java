package no.nav.syfo.pdl;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class PdlPerson {
    public List<PdlPersonNavn> navn;
}
