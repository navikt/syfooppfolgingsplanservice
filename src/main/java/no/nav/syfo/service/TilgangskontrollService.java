package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class TilgangskontrollService {

    private static final Logger log = getLogger(TilgangskontrollService.class);

    private NarmesteLederConsumer narmesteLederConsumer;
    private AktorregisterConsumer aktorregisterConsumer;

    @Inject
    public TilgangskontrollService(
            NarmesteLederConsumer narmesteLederConsumer,
            AktorregisterConsumer aktorregisterConsumer
    ) {
        this.narmesteLederConsumer = narmesteLederConsumer;
        this.aktorregisterConsumer = aktorregisterConsumer;
    }

    public boolean brukerTilhorerOppfolgingsplan(String fnr, Oppfolgingsplan oppfolgingsplan) {
        String arbeidstakersFnr = aktorregisterConsumer.hentFnrForAktor(oppfolgingsplan.arbeidstaker.aktoerId);
        return arbeidstakersFnr.equals(fnr)
                || erNaermesteLederForSykmeldt(fnr, arbeidstakersFnr, oppfolgingsplan.virksomhet.virksomhetsnummer);
    }

    public boolean kanOppretteOppfolgingsplan(String sykmeldtFnr, String innloggetFnr, String virksomhetsnummer) {
        boolean innloggetBrukerErSykmeldt = innloggetFnr.equals(sykmeldtFnr);
        boolean aktoerHarNaermesteLederHosVirksomhet = aktoerHarNaermesteLederHosVirksomhet(innloggetFnr, virksomhetsnummer);
        boolean erNaermesteLederForSykmeldt = erNaermesteLederForSykmeldt(innloggetFnr, sykmeldtFnr, virksomhetsnummer);
        log.info("kanOppretteOppfolgingsplan: " + innloggetBrukerErSykmeldt + " " + aktoerHarNaermesteLederHosVirksomhet + " " + erNaermesteLederForSykmeldt);
        return (innloggetBrukerErSykmeldt && aktoerHarNaermesteLederHosVirksomhet)
                || erNaermesteLederForSykmeldt;
    }

    private boolean erNaermesteLederForSykmeldt(String lederFnr, String sykmeldtFnr, String virksomhetsnummer) {
        return narmesteLederConsumer.ansatte(lederFnr).stream()
                .anyMatch(ansatt -> virksomhetsnummer.equals(ansatt.virksomhetsnummer) && ansatt.fnr.equals(sykmeldtFnr));
    }

    private boolean aktoerHarNaermesteLederHosVirksomhet(String fnr, String virksomhetsnummer) {
        return narmesteLederConsumer.narmesteLeder(fnr, virksomhetsnummer).isPresent();
    }
}
