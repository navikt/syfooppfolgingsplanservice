package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(fluent = true)
public class Sykmelding implements Serializable {
    public String id;
    public List<Sykmeldingsperiode> sykmeldingsperioder;
    public OrganisasjonsInformasjon organisasjonsInformasjon;

    public Sykmelding(String id, List<Sykmeldingsperiode> sykmeldingsperioder, OrganisasjonsInformasjon organisasjonsInformasjon) {
        this.id = id;
        this.sykmeldingsperioder = sykmeldingsperioder;
        this.organisasjonsInformasjon = organisasjonsInformasjon;
    }
}
