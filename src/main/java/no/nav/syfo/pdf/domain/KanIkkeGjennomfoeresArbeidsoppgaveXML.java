package no.nav.syfo.pdf.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "kanikkesarbeidsoppgave",
        propOrder = {
                "navn",
                "beskrivelse",
        }
)
public class KanIkkeGjennomfoeresArbeidsoppgaveXML {

    public String navn;
    public String beskrivelse;

    public KanIkkeGjennomfoeresArbeidsoppgaveXML withNavn(String navn) {
        this.navn = navn;
        return this;
    }

    public KanIkkeGjennomfoeresArbeidsoppgaveXML withBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }
}
