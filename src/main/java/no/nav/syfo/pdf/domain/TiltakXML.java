package no.nav.syfo.pdf.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "tiltak",
        propOrder = {
                "id",
                "navn",
                "beskrivelse",
                "opprettetAv",
                "fom",
                "tom",
                "status",
                "gjennomfoering",
                "beskrivelseIkkeAktuelt",
        }
)
public class TiltakXML {
    public Long id;
    public String navn;
    public String beskrivelse;
    public String opprettetAv;
    public String fom;
    public String tom;
    public String status;
    public String gjennomfoering;
    public String beskrivelseIkkeAktuelt;

    public TiltakXML withFom(String fom) {
        this.fom = fom;
        return this;
    }

    public TiltakXML withTom(String tom) {
        this.tom = tom;
        return this;
    }

    public TiltakXML withStatus(String status) {
        this.status = status;
        return this;
    }

    public TiltakXML withGjennomfoering(String gjennomfoering) {
        this.gjennomfoering = gjennomfoering;
        return this;
    }

    public TiltakXML withBeskrivelseIkkeAktuelt(String beskrivelseIkkeAktuelt) {
        this.beskrivelseIkkeAktuelt = beskrivelseIkkeAktuelt;
        return this;
    }

    public TiltakXML withNavn(String navn) {
        this.navn = navn;
        return this;
    }

    public TiltakXML withBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }

    public TiltakXML withOpprettetAv(String opprettetAv) {
        this.opprettetAv = opprettetAv;
        return this;
    }

    public TiltakXML withId(Long id) {
        this.id = id;
        return this;
    }
}
