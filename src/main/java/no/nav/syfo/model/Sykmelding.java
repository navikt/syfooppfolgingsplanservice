package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class Sykmelding {
    String id;
    List<Sykmeldingsperiode> sykmeldingsperioder;
    OrganisasjonsInformasjon organisasjonsInformasjon;

    public Sykmelding(String id, List<Sykmeldingsperiode> sykmeldingsperioder, OrganisasjonsInformasjon organisasjonsInformasjon) {
        this.id = id;
        this.sykmeldingsperioder = sykmeldingsperioder;
        this.organisasjonsInformasjon = organisasjonsInformasjon;
    }
}
