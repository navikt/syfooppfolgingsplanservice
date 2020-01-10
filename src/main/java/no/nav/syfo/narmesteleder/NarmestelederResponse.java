package no.nav.syfo.narmesteleder;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class NarmestelederResponse {
    public NarmesteLederRelasjon narmesteLederRelasjon;
}
