package no.nav.syfo.pdf.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "sykeforloepsperioder",
        propOrder = {
                "fom",
                "tom",
                "antallDager",
                "gradering",
                "behandlingsdager",
                "reisetilskudd",
                "avventende"
        }
)
public class SykeforloepsperioderXML {
    public String fom;
    public String tom;
    public int antallDager;
    public int gradering;
    public Boolean behandlingsdager;
    public Boolean reisetilskudd;
    public Boolean avventende;

    public SykeforloepsperioderXML withFom(String fom) {
        this.fom = fom;
        return this;
    }

    public SykeforloepsperioderXML withTom(String tom) {
        this.tom = tom;
        return this;
    }

    public SykeforloepsperioderXML withAntallDager(int antallDager) {
        this.antallDager = antallDager;
        return this;
    }

    public SykeforloepsperioderXML withGradering(int gradering) {
        this.gradering = gradering;
        return this;
    }

    public SykeforloepsperioderXML withBehandlingsdager(Boolean behandlingsdager) {
        this.behandlingsdager = behandlingsdager;
        return this;
    }

    public SykeforloepsperioderXML withReisetilskudd(Boolean reisetilskudd) {
        this.reisetilskudd = reisetilskudd;
        return this;
    }

    public SykeforloepsperioderXML withAvventende(Boolean avventende) {
        this.avventende = avventende;
        return this;
    }

}
