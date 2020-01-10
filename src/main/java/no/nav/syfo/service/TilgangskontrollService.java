package no.nav.syfo.service;

import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;

@Service
public class TilgangskontrollService {

    private NarmesteLederConsumer narmesteLederConsumer;

    @Inject
    public TilgangskontrollService(
            NarmesteLederConsumer narmesteLederConsumer
    ) {
        this.narmesteLederConsumer = narmesteLederConsumer;
    }

    public boolean aktoerTilhoererDialogen(String aktoerId, Oppfoelgingsdialog oppfoelgingsdialog) {
        return oppfoelgingsdialog.arbeidstaker.aktoerId.equals(aktoerId)
                || erAktoerNaermestelederForBruker(aktoerId, oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer);
    }

    public boolean kanOppretteDialog(String sykmeldtAktoerId, String aktoerId, String virksomhetsnummer) {
        return (aktoerId.equals(sykmeldtAktoerId) && aktoerHarNaermesteLederHosVirksomhet(aktoerId, virksomhetsnummer))
                || erAktoerNaermestelederForBruker(aktoerId, sykmeldtAktoerId, virksomhetsnummer);
    }

    private boolean erAktoerNaermestelederForBruker(String aktoerId, String sykmeldtAktoerId, String virksomhetsnummer) {
        return narmesteLederConsumer.ansatte(aktoerId).stream()
                .anyMatch(ansatt -> virksomhetsnummer.equals(ansatt.virksomhetsnummer) && ansatt.aktoerId.equals(sykmeldtAktoerId));
    }

    private boolean aktoerHarNaermesteLederHosVirksomhet(String aktoerId, String virksomhetsnummer) {
        return narmesteLederConsumer.narmesteLeder(aktoerId, virksomhetsnummer).isPresent();
    }
}
