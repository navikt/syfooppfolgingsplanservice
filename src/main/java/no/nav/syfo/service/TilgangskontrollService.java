package no.nav.syfo.service;

import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.pdl.PdlConsumer;

import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class TilgangskontrollService {

    private NarmesteLederConsumer narmesteLederConsumer;
    private PdlConsumer pdlConsumer;

    @Inject
    public TilgangskontrollService(
            NarmesteLederConsumer narmesteLederConsumer,
            PdlConsumer pdlConsumer
    ) {
        this.narmesteLederConsumer = narmesteLederConsumer;
        this.pdlConsumer = pdlConsumer;
    }

    public boolean brukerTilhorerOppfolgingsplan(String fnr, Oppfolgingsplan oppfolgingsplan) {
        String arbeidstakersFnr = pdlConsumer.fnr(oppfolgingsplan.arbeidstaker.aktoerId);
        return arbeidstakersFnr.equals(fnr)
                || erNaermesteLederForSykmeldt(fnr, arbeidstakersFnr, oppfolgingsplan.virksomhet.virksomhetsnummer);
    }

    public boolean kanOppretteOppfolgingsplan(String sykmeldtFnr, String innloggetFnr, String virksomhetsnummer) {
        return (innloggetFnr.equals(sykmeldtFnr) && aktoerHarNaermesteLederHosVirksomhet(innloggetFnr, virksomhetsnummer))
                || erNaermesteLederForSykmeldt(innloggetFnr, sykmeldtFnr, virksomhetsnummer);
    }

    public boolean erNaermesteLederForSykmeldt(String lederFnr, String sykmeldtFnr, String virksomhetsnummer) {
        return narmesteLederConsumer.ansatte(lederFnr).stream()
                .anyMatch(ansatt -> virksomhetsnummer.equals(ansatt.virksomhetsnummer) && ansatt.fnr.equals(sykmeldtFnr));
    }

    private boolean aktoerHarNaermesteLederHosVirksomhet(String fnr, String virksomhetsnummer) {
        return narmesteLederConsumer.narmesteLeder(fnr, virksomhetsnummer).isPresent();
    }
}
