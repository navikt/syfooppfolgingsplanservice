package no.nav.syfo.pdf.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

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
