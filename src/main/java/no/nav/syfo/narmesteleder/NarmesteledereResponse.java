package no.nav.syfo.narmesteleder;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(fluent = true)
public class NarmesteledereResponse {
    public List<NarmesteLederRelasjon> narmesteLederRelasjoner;
}
