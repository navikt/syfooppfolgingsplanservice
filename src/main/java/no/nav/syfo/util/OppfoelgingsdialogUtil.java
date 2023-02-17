package no.nav.syfo.util;

import no.nav.syfo.api.selvbetjening.domain.RSBrukerOppfolgingsplan;
import no.nav.syfo.domain.Arbeidsoppgave;
import no.nav.syfo.domain.Godkjenning;
import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.domain.Tiltak;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.*;

public class OppfoelgingsdialogUtil {


    public static List<Godkjenning> fjernEldsteGodkjenning(List<Godkjenning> godkjenninger) {
        if (godkjenninger.size() == 1) {
            return emptyList();
        }
        return godkjenninger.stream()
                .sorted(comparing(o -> o.godkjenningsTidspunkt))
                .collect(toList()).subList(1, godkjenninger.size());
    }

    public static List<Arbeidsoppgave> finnIkkeTattStillingTilArbeidsoppgaver(List<Arbeidsoppgave> arbeidsoppgaveListe) {
        return arbeidsoppgaveListe.stream()
                .filter(arbeidsoppgave -> !arbeidsoppgave.erVurdertAvSykmeldt)
                .collect(toList());
    }

    public static List<Arbeidsoppgave> finnKanGjennomfoeresArbeidsoppgaver(List<Arbeidsoppgave> arbeidsoppgaveListe) {
        return arbeidsoppgaveListe.stream()
                .filter(arbeidsoppgave -> KAN.name().equals(arbeidsoppgave.gjennomfoering.gjennomfoeringStatus))
                .collect(toList());
    }

    public static List<Arbeidsoppgave> finnKanIkkeGjennomfoeresArbeidsoppgaver(List<Arbeidsoppgave> arbeidsoppgaveListe) {
        return arbeidsoppgaveListe.stream()
                .filter(arbeidsoppgave -> KAN_IKKE.name().equals(arbeidsoppgave.gjennomfoering.gjennomfoeringStatus))
                .collect(toList());
    }

    public static List<Arbeidsoppgave> finnKanGjennomfoeresMedTilretteleggingArbeidsoppgaver(List<Arbeidsoppgave> arbeidsoppgaveListe) {
        return arbeidsoppgaveListe.stream()
                .filter(arbeidsoppgave -> TILRETTELEGGING.name().equals(arbeidsoppgave.gjennomfoering.gjennomfoeringStatus))
                .collect(toList());
    }

    public static boolean erGodkjentAvAnnenPart(Oppfolgingsplan oppfolgingsplan, String aktoerId) {
        if (erArbeidstakeren(oppfolgingsplan, aktoerId)) {
            return harArbeidsgiverGodkjentPlan(oppfolgingsplan);
        }
        return harArbeidstakerGodkjentPlan(oppfolgingsplan);
    }

    public static boolean annenPartHarGjortEndringerImellomtiden(Oppfolgingsplan oppfolgingsplan, String innloggetAktoerId) {
        if (erArbeidstakeren(oppfolgingsplan, innloggetAktoerId)) {
            return oppfolgingsplan.sistEndretArbeidsgiver != null && oppfolgingsplan.arbeidstaker.sistAksessert != null && oppfolgingsplan.arbeidstaker.sistAksessert.isBefore(oppfolgingsplan.sistEndretArbeidsgiver);
        }
        return oppfolgingsplan.sistEndretSykmeldt != null && oppfolgingsplan.arbeidsgiver.sistAksessert != null && oppfolgingsplan.arbeidsgiver.sistAksessert.isBefore(oppfolgingsplan.sistEndretSykmeldt);

    }

    public static Godkjenning sisteGodkjenningAvAnnenPart(Oppfolgingsplan oppfolgingsplan, String innloggetAktoerId) {
        if (erArbeidstakeren(oppfolgingsplan, innloggetAktoerId)) {
            return oppfolgingsplan.godkjenninger.stream()
                    .filter(godkjenning -> godkjenning.godkjentAvAktoerId.equals(innloggetAktoerId))
                    .sorted((o1, o2) -> o2.godkjenningsTidspunkt.compareTo(o1.godkjenningsTidspunkt)).findFirst().get();
        }
        return oppfolgingsplan.godkjenninger.stream()
                .filter(godkjenning -> !godkjenning.godkjentAvAktoerId.equals(innloggetAktoerId))
                .sorted((o1, o2) -> o2.godkjenningsTidspunkt.compareTo(o1.godkjenningsTidspunkt)).findFirst().get();
    }


    public static boolean harArbeidsgiverGodkjentPlan(Oppfolgingsplan oppfolgingsplan) {
        return oppfolgingsplan.godkjenninger.stream().anyMatch(godkjenning -> !erArbeidstakeren(oppfolgingsplan, godkjenning.godkjentAvAktoerId) && godkjenning.godkjent);
    }

    public static boolean harArbeidstakerGodkjentPlan(Oppfolgingsplan oppfolgingsplan) {
        return oppfolgingsplan.godkjenninger.stream().anyMatch(godkjenning -> erArbeidstakeren(oppfolgingsplan, godkjenning.godkjentAvAktoerId) && godkjenning.godkjent);
    }

    public static boolean erArbeidstakeren(Oppfolgingsplan oppfolgingsplan, String aktoerId) {
        return aktoerId.equals(oppfolgingsplan.arbeidstaker.aktoerId);
    }

    public static boolean erArbeidsgiveren(Oppfolgingsplan oppfolgingsplan, String aktoerId) {
        return !erArbeidstakeren(oppfolgingsplan, aktoerId);
    }

    public static boolean isLoggedInpersonLeaderAndOwnLeader(Oppfolgingsplan oppfolgingsplan, String loggedInPersonFnr, String leaderFnr) {
        return oppfolgingsplan.arbeidstaker.fnr.equals(leaderFnr) && loggedInPersonFnr.equals(leaderFnr);
    }

    public static boolean eksisterendeTiltakHoererTilDialog(Long tiltakId, List<Tiltak> tiltakListe) {
        return tiltakId == null || tiltakListe.stream().anyMatch(tiltak -> tiltak.id.equals(tiltakId));
    }

    public static boolean eksisterendeArbeidsoppgaveHoererTilDialog(Long arbeidsoppgaveId, List<Arbeidsoppgave> arbeidsoppgaveListe) {
        return arbeidsoppgaveId == null || arbeidsoppgaveListe.stream().anyMatch(arbeidsoppgave -> arbeidsoppgave.id.equals(arbeidsoppgaveId));
    }

    public static List<RSBrukerOppfolgingsplan> populerOppfolgingsplanerMedAvbruttPlanListe(List<RSBrukerOppfolgingsplan> planer) {
        return planer.stream()
                .map(rsOppfoelgingsdialog -> rsOppfoelgingsdialog.avbruttPlanListe(planer
                        .stream()
                        .filter(oppfoelgingsdialog -> oppfoelgingsdialog.arbeidstaker.fnr.equals(rsOppfoelgingsdialog.arbeidstaker.fnr) &&
                                oppfoelgingsdialog.virksomhet.virksomhetsnummer.equals(rsOppfoelgingsdialog.virksomhet.virksomhetsnummer) &&
                                oppfoelgingsdialog.godkjentPlan != null)
                        .filter(oppfoelgingsdialog -> oppfoelgingsdialog.godkjentPlan.avbruttPlan != null)
                        .filter(oppfoelgingsdialog -> oppfoelgingsdialog.opprettetDato.isBefore(rsOppfoelgingsdialog.opprettetDato))
                        .sorted((o1, o2) -> o2.godkjentPlan.avbruttPlan.tidspunkt.compareTo(o1.godkjentPlan.avbruttPlan.tidspunkt))
                        .map(oppfoelgingsdialog -> oppfoelgingsdialog.godkjentPlan.avbruttPlan
                                .id(oppfoelgingsdialog.id))
                        .collect(toList())
                ))
                .collect(toList());
    }
}
