package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(fluent = true)
public class Sykmelding implements Serializable {
    public String id;
    public String fnr;
    public List<Sykmeldingsperiode> sykmeldingsperioder;
    public Organisasjonsinformasjon organisasjonsinformasjon;

    public Sykmelding(String id, String fnr, List<Sykmeldingsperiode> sykmeldingsperioder, Organisasjonsinformasjon organisasjonsinformasjon) {
        this.id = id;
        this.fnr = fnr;
        this.sykmeldingsperioder = sykmeldingsperioder;
        this.organisasjonsinformasjon = organisasjonsinformasjon;
    }
}
