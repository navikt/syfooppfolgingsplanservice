package no.nav.syfo.pdf.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "tilretteleggesarbeidsoppgave",
        propOrder = {
                "navn",
                "beskrivelse",
                "paaAnnetSted",
                "medMerTid",
                "medHjelp",
        }
)
public class KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML {

    public String navn;
    public String beskrivelse;
    public boolean paaAnnetSted;
    public boolean medMerTid;
    public boolean medHjelp;


    public KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML withNavn(String navn) {
        this.navn = navn;
        return this;
    }

    public KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML withBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }

    public KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML withPaaAnnetSted(boolean paaAnnetSted) {
        this.paaAnnetSted = paaAnnetSted;
        return this;
    }

    public KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML withMedMerTid(boolean medMerTid) {
        this.medMerTid = medMerTid;
        return this;
    }

    public KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML withMedHjelp(boolean medHjelp) {
        this.medHjelp = medHjelp;
        return this;
    }
}
