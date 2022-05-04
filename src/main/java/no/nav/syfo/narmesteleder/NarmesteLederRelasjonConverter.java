package no.nav.syfo.narmesteleder;

import org.springframework.stereotype.Component;

import no.nav.syfo.model.NaermesteLederStatus;
import no.nav.syfo.model.Naermesteleder;

@Component
public class NarmesteLederRelasjonConverter {

    protected Naermesteleder convert(NarmesteLederRelasjon narmesteLederRelasjon, String lederNavn) {
        return new Naermesteleder()
                .epost(narmesteLederRelasjon.narmesteLederEpost)
                .mobil(narmesteLederRelasjon.narmesteLederTelefonnummer)
                .naermesteLederStatus(
                        new NaermesteLederStatus()
                                .erAktiv(narmesteLederRelasjon.aktivTom == null)
                                .aktivFom(narmesteLederRelasjon.aktivFom)
                                .aktivTom(narmesteLederRelasjon.aktivTom))
                .orgnummer(narmesteLederRelasjon.orgnummer)
                .navn(lederNavn)
                .ansattFnr(narmesteLederRelasjon.fnr)
                .naermesteLederFnr(narmesteLederRelasjon.narmesteLederFnr);
    }
}
