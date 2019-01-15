package no.nav.syfo.pdf.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "normalArbeidsoppgave",
        propOrder = {
                "navn",
        }
)
public class KanGjennomfoeresArbeidsoppgaveXML {

    public String navn;

    public KanGjennomfoeresArbeidsoppgaveXML withNavn(String navn) {
        this.navn = navn;
        return this;
    }
}
