package no.nav.syfo.service;

import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class TilgangskontrollService {

    private NarmesteLederConsumer narmesteLederConsumer;

    @Inject
    public TilgangskontrollService(
            NarmesteLederConsumer narmesteLederConsumer
    ) {
        this.narmesteLederConsumer = narmesteLederConsumer;
    }

    public boolean brukerTilhorerOppfolgingsplan(String fnr, Oppfolgingsplan oppfolgingsplan) {
        return oppfolgingsplan.arbeidstaker.fnr.equals(fnr)
                || erNaermesteLederForSykmeldt(fnr, oppfolgingsplan.arbeidstaker.fnr, oppfolgingsplan.virksomhet.virksomhetsnummer);
    }

    public boolean kanOppretteOppfolgingsplan(String sykmeldtAktoerId, String aktoerId, String sykmeldtFnr, String lederFnr, String virksomhetsnummer) {
        return (aktoerId.equals(sykmeldtAktoerId) && aktoerHarNaermesteLederHosVirksomhet(aktoerId, virksomhetsnummer))
                || erNaermesteLederForSykmeldt(lederFnr, sykmeldtFnr, virksomhetsnummer);
    }

    private boolean erNaermesteLederForSykmeldt(String lederFnr, String sykmeldtFnr, String virksomhetsnummer) {
        return narmesteLederConsumer.ansatte(lederFnr).stream()
                .anyMatch(ansatt -> virksomhetsnummer.equals(ansatt.virksomhetsnummer) && ansatt.fnr.equals(sykmeldtFnr));
    }

    private boolean aktoerHarNaermesteLederHosVirksomhet(String aktoerId, String virksomhetsnummer) {
        return narmesteLederConsumer.narmesteLeder(aktoerId, virksomhetsnummer).isPresent();
    }
}
