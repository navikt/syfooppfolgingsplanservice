package no.nav.syfo.pdf.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "oppfoelgingsdialogXML",
        propOrder = {
                "arbeidsgivernavn",
                "virksomhetsnavn",
                "sykmeldtnavn",
                "gyldigfra",
                "gyldigtil",
                "evalueres",
                "visAdvarsel",
                "sykmeldtFnr",
                "sykmeldtTlf",
                "sykmeldtEpost",
                "arbeidsgiverOrgnr",
                "arbeidsgiverTlf",
                "arbeidsgiverEpost",
                "ikkeTattStillingTilArbeidsoppgaveListe",
                "kanGjennomfoeresArbeidsoppgaveListe",
                "kanGjennomfoeresMedTilretteleggingArbeidsoppgaveListe",
                "kanIkkeGjennomfoeresArbeidsoppgaveListe",
                "tiltakListe",
                "godkjentAv",
                "opprettetAv",
                "opprettetDato",
                "godkjentDato",
                "fotnote",
                "stillingListe",
        }
)
@XmlRootElement(
        name = "oppfoelgingsdialogXML"
)
public class OppfoelgingsdialogXML {

    public boolean visAdvarsel;
    public String opprettetAv;
    public String fotnote;
    public String godkjentAv;
    public String opprettetDato;
    public String godkjentDato;
    public String arbeidsgivernavn;
    public String virksomhetsnavn;
    public String sykmeldtnavn;
    public String gyldigfra;
    public String gyldigtil;
    public String evalueres;
    public String sykmeldtFnr;
    public String sykmeldtTlf;
    public String sykmeldtEpost;
    public String arbeidsgiverOrgnr;
    public String arbeidsgiverTlf;
    public String arbeidsgiverEpost;
    public List<IkkeTattStillingTilArbeidsoppgaveXML> ikkeTattStillingTilArbeidsoppgaveListe = new ArrayList<>();
    public List<KanGjennomfoeresArbeidsoppgaveXML> kanGjennomfoeresArbeidsoppgaveListe = new ArrayList<>();
    public List<KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML> kanGjennomfoeresMedTilretteleggingArbeidsoppgaveListe = new ArrayList<>();
    public List<KanIkkeGjennomfoeresArbeidsoppgaveXML> kanIkkeGjennomfoeresArbeidsoppgaveListe = new ArrayList<>();
    public List<TiltakXML> tiltakListe = new ArrayList<>();
    public List<StillingXML> stillingListe = new ArrayList<>();

    public OppfoelgingsdialogXML withVisAdvarsel(boolean visAdvarsel) {
        this.visAdvarsel = visAdvarsel;
        return this;
    }

    public OppfoelgingsdialogXML withArbeidsgivernavn(String arbeidsgivernavn) {
        this.arbeidsgivernavn = arbeidsgivernavn;
        return this;
    }

    public OppfoelgingsdialogXML withVirksomhetsnavn(String virksomhetsnavn) {
        this.virksomhetsnavn = virksomhetsnavn;
        return this;
    }

    public OppfoelgingsdialogXML withSykmeldtNavn(String sykmeldtnavn) {
        this.sykmeldtnavn = sykmeldtnavn;
        return this;
    }

    public OppfoelgingsdialogXML withGyldigfra(String gyldigfra) {
        this.gyldigfra = gyldigfra;
        return this;
    }

    public OppfoelgingsdialogXML withGyldigtil(String gyldigtil) {
        this.gyldigtil = gyldigtil;
        return this;
    }

    public OppfoelgingsdialogXML withSykmeldtEpost(String sykmeldtEpost) {
        this.sykmeldtEpost = sykmeldtEpost;
        return this;
    }

    public OppfoelgingsdialogXML withEvalueres(String evalueres) {
        this.evalueres = evalueres;
        return this;
    }

    public OppfoelgingsdialogXML withArbeidsgiverTlf(String arbeidsgiverTlf) {
        this.arbeidsgiverTlf = arbeidsgiverTlf;
        return this;
    }
    public OppfoelgingsdialogXML withSykmeldtTlf(String sykmeldtTlf) {
        this.sykmeldtTlf = sykmeldtTlf;
        return this;
    }

    public OppfoelgingsdialogXML withSykmeldtFnr(String sykmeldtFnr) {
        this.sykmeldtFnr = sykmeldtFnr;
        return this;
    }
    public OppfoelgingsdialogXML withArbeidsgiverOrgnr(String arbeidsgiverOrgnr) {
        this.arbeidsgiverOrgnr = arbeidsgiverOrgnr;
        return this;
    }
    public OppfoelgingsdialogXML withArbeidsgiverEpost(String arbeidsgiverEpost) {
        this.arbeidsgiverEpost = arbeidsgiverEpost;
        return this;
    }

    public OppfoelgingsdialogXML withKanGjennomfoeresArbeidsoppgaveXMLList(List<KanGjennomfoeresArbeidsoppgaveXML> kanGjennomfoeresArbeidsoppgaveListe) {
        this.kanGjennomfoeresArbeidsoppgaveListe = kanGjennomfoeresArbeidsoppgaveListe;
        return this;
    }

    public OppfoelgingsdialogXML withKanGjennomfoeresMedTilretteleggingArbeidsoppgaveXMLList(List<KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML> kanGjennomfoeresMedTilretteleggingArbeidsoppgaveListe) {
        this.kanGjennomfoeresMedTilretteleggingArbeidsoppgaveListe = kanGjennomfoeresMedTilretteleggingArbeidsoppgaveListe;
        return this;
    }

    public OppfoelgingsdialogXML withKanIkkeGjennomfoeresArbeidsoppgaveXMLList(List<KanIkkeGjennomfoeresArbeidsoppgaveXML> kanIkkeGjennomfoeresArbeidsoppgaveListe) {
        this.kanIkkeGjennomfoeresArbeidsoppgaveListe = kanIkkeGjennomfoeresArbeidsoppgaveListe;
        return this;
    }

    public OppfoelgingsdialogXML withIkkeTattStillingTilArbeidsoppgaveXML(List<IkkeTattStillingTilArbeidsoppgaveXML> ikkeTattStillingTilArbeidsoppgaveListe) {
        this.ikkeTattStillingTilArbeidsoppgaveListe = ikkeTattStillingTilArbeidsoppgaveListe;
        return this;
    }

    public OppfoelgingsdialogXML withStillingListe(List<StillingXML> stillingListe) {
        this.stillingListe = stillingListe;
        return this;
    }

    public OppfoelgingsdialogXML withTiltak(List<TiltakXML> tiltakListe) {
        this.tiltakListe = tiltakListe;
        return this;
    }

    public OppfoelgingsdialogXML withOpprettetAv(String opprettetAv) {
        this.opprettetAv = opprettetAv;
        return this;
    }

    public OppfoelgingsdialogXML withOpprettetDato(String opprettetDato) {
        this.opprettetDato = opprettetDato;
        return this;
    }

    public OppfoelgingsdialogXML withGodkjentAv(String godkjentAv) {
        this.godkjentAv = godkjentAv;
        return this;
    }

    public OppfoelgingsdialogXML withGodkjentDato(String godkjentDato) {
        this.godkjentDato = godkjentDato;
        return this;
    }

    public OppfoelgingsdialogXML withFotnote(String fotnote) {
        this.fotnote = fotnote;
        return this;
    }
}
