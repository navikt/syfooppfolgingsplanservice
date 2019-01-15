package no.nav.syfo.domain.sykmelding;

import java.time.LocalDate;

public class Periode {
    public LocalDate fom;
    public LocalDate tom;
    public Integer grad;
    public Boolean behandlingsdager;
    public Boolean reisetilskudd;
    public Boolean avventende;

    public Periode withFom(LocalDate fom) {
        this.fom = fom;
        return this;
    }

    public Periode withTom(LocalDate tom) {
        this.tom = tom;
        return this;
    }

    public Periode withGrad(Integer grad) {
        this.grad = grad;
        return this;
    }

    public Periode withBehandlingsdager(Boolean behandlingsdager) {
        this.behandlingsdager = behandlingsdager;
        return this;
    }

    public Periode withReisetilskudd(Boolean reisetilskudd) {
        this.reisetilskudd = reisetilskudd;
        return this;
    }

    public Periode withAvventende(Boolean avventende) {
        this.avventende = avventende;
        return this;
    }

}