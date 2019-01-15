package no.nav.syfo.util;

import no.nav.syfo.domain.Arbeidsoppgave;
import no.nav.syfo.domain.Godkjenning;
import no.nav.syfo.domain.Oppfoelgingsdialog;
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

    public static boolean erGodkjentAvAnnenPart(Oppfoelgingsdialog oppfoelgingsdialog, String aktoerId) {
        if (erArbeidstakeren(oppfoelgingsdialog, aktoerId)) {
            return harArbeidsgiverGodkjentPlan(oppfoelgingsdialog);
        }
        return harArbeidstakerGodkjentPlan(oppfoelgingsdialog);
    }

    public static boolean annenPartHarGjortEndringerImellomtiden(Oppfoelgingsdialog oppfoelgingsdialog, String innloggetAktoerId) {
        if (erArbeidstakeren(oppfoelgingsdialog, innloggetAktoerId)) {
            return oppfoelgingsdialog.sistEndretArbeidsgiver != null && oppfoelgingsdialog.arbeidstaker.sistAksessert != null && oppfoelgingsdialog.arbeidstaker.sistAksessert.isBefore(oppfoelgingsdialog.sistEndretArbeidsgiver);
        }
        return oppfoelgingsdialog.sistEndretSykmeldt != null && oppfoelgingsdialog.arbeidsgiver.sistAksessert != null && oppfoelgingsdialog.arbeidsgiver.sistAksessert.isBefore(oppfoelgingsdialog.sistEndretSykmeldt);

    }

    public static Godkjenning sisteGodkjenningAvAnnenPart(Oppfoelgingsdialog oppfoelgingsdialog, String innloggetAktoerId) {
        if (erArbeidstakeren(oppfoelgingsdialog, innloggetAktoerId)) {
            return oppfoelgingsdialog.godkjenninger.stream()
                    .filter(godkjenning -> godkjenning.godkjentAvAktoerId.equals(innloggetAktoerId))
                    .sorted((o1, o2) -> o2.godkjenningsTidspunkt.compareTo(o1.godkjenningsTidspunkt)).findFirst().get();
        }
        return oppfoelgingsdialog.godkjenninger.stream()
                .filter(godkjenning -> !godkjenning.godkjentAvAktoerId.equals(innloggetAktoerId))
                .sorted((o1, o2) -> o2.godkjenningsTidspunkt.compareTo(o1.godkjenningsTidspunkt)).findFirst().get();
    }


    public static boolean harArbeidsgiverGodkjentPlan(Oppfoelgingsdialog oppfoelgingsdialog) {
        return oppfoelgingsdialog.godkjenninger.stream().anyMatch(godkjenning -> !erArbeidstakeren(oppfoelgingsdialog, godkjenning.godkjentAvAktoerId) && godkjenning.godkjent);
    }

    public static boolean harArbeidstakerGodkjentPlan(Oppfoelgingsdialog oppfoelgingsdialog) {
        return oppfoelgingsdialog.godkjenninger.stream().anyMatch(godkjenning -> erArbeidstakeren(oppfoelgingsdialog, godkjenning.godkjentAvAktoerId) && godkjenning.godkjent);
    }

    public static boolean erArbeidstakeren(Oppfoelgingsdialog oppfoelgingsdialog, String aktoerId) {
        return aktoerId.equals(oppfoelgingsdialog.arbeidstaker.aktoerId);
    }

    public static boolean erArbeidsgiveren(Oppfoelgingsdialog oppfoelgingsdialog, String aktoerId) {
        return !erArbeidstakeren(oppfoelgingsdialog, aktoerId);
    }

    public static boolean eksisterendeTiltakHoererTilDialog(Long tiltakId, List<Tiltak> tiltakListe) {
        return tiltakId == null || tiltakListe.stream().anyMatch(tiltak -> tiltak.id.equals(tiltakId));
    }

    public static boolean eksisterendeArbeidsoppgaveHoererTilDialog(Long arbeidsoppgaveId, List<Arbeidsoppgave> arbeidsoppgaveListe) {
        return arbeidsoppgaveId == null || arbeidsoppgaveListe.stream().anyMatch(arbeidsoppgave -> arbeidsoppgave.id.equals(arbeidsoppgaveId));
    }
}
