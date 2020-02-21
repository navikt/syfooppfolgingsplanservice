package no.nav.syfo.pdl;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(fluent = true)
public class PdlPerson implements Serializable {
    public List<PdlPersonNavn> navn;
}
