package no.nav.syfo.pdf.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "ikkeTattStillingTilArbeidsoppgave",
        propOrder = {
                "navn",
        }
)
public class IkkeTattStillingTilArbeidsoppgaveXML {

    public String navn;

    public IkkeTattStillingTilArbeidsoppgaveXML withNavn(String navn) {
        this.navn = navn;
        return this;
    }
}

