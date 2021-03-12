package no.nav.syfo.narmesteleder;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class NermesteledereResponse {
    public List<NarmesteLederRelasjon> narmesteLederRelasjoner;
}
