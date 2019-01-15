package no.nav.syfo.service;

import no.nav.syfo.domain.Oppfoelgingsdialog;

import javax.inject.Inject;

import static java.lang.System.getProperty;

public class TilgangskontrollService {

    @Inject
    private NaermesteLederService naermesteLederService;

    public boolean aktoerTilhoererDialogen(String aktoerId, Oppfoelgingsdialog oppfoelgingsdialog) {
        return "true".equals(getProperty("disable.tilgangskontroll")) || oppfoelgingsdialog.arbeidstaker.aktoerId.equals(aktoerId) || erAktoerNaermestelederForBruker(aktoerId, oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer);
    }

    public boolean kanOppretteDialog(String sykmeldtAktoerId, String aktoerId, String virksomhetsnummer) {
        return "true".equals(getProperty("disable.tilgangskontroll")) || (aktoerId.equals(sykmeldtAktoerId) && aktoerHarNaermesteLederHosVirksomhet(aktoerId, virksomhetsnummer)) || erAktoerNaermestelederForBruker(aktoerId, sykmeldtAktoerId, virksomhetsnummer);
    }

    private boolean erAktoerNaermestelederForBruker(String aktoerId, String sykmeldtAktoerId, String virksomhetsnummer) {
        return naermesteLederService.hentAnsatte(aktoerId).stream()
                .anyMatch(ansatt -> virksomhetsnummer.equals(ansatt.virksomhetsnummer) && ansatt.aktoerId.equals(sykmeldtAktoerId));
    }

    private boolean aktoerHarNaermesteLederHosVirksomhet(String aktoerId, String virksomhetsnummer) {
        return naermesteLederService.hentNaermesteLeder(aktoerId, virksomhetsnummer).isPresent();
    }
}
