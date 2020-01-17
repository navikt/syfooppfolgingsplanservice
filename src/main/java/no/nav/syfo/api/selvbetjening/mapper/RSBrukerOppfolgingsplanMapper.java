package no.nav.syfo.api.selvbetjening.mapper;

import no.nav.syfo.api.selvbetjening.domain.*;
import no.nav.syfo.domain.*;

import java.time.LocalDate;
import java.util.function.Function;

import static no.nav.syfo.api.intern.mappers.OppfoelgingsdialogRestMapper.status2rs;
import static no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.*;
import static no.nav.syfo.service.AktoerService.aktoerService;
import static no.nav.syfo.util.MapUtil.*;

public class RSBrukerOppfolgingsplanMapper {

    private static Function<Gyldighetstidspunkt, RSGyldighetstidspunkt> gyldighetstidspunkt2rs = gyldighetstidspunkt -> {
        if (gyldighetstidspunkt.fom != null && gyldighetstidspunkt.tom != null && gyldighetstidspunkt.evalueres != null) {
            return new RSGyldighetstidspunkt()
                    .fom(gyldighetstidspunkt.fom)
                    .tom(gyldighetstidspunkt.tom)
                    .evalueres(gyldighetstidspunkt.evalueres);
        } else {
            return null;
        }
    };

    private static Function<GodkjentPlan, RSAvbruttplan> avbruttplan2rs = godkjentPlan ->
            godkjentPlan.avbruttPlan
                    .map(avbruttPlan -> new RSAvbruttplan()
                            .av(new RSPerson().fnr(avbruttPlan.avAktoerId != null ? aktoerService().hentFnrForAktoer(avbruttPlan.avAktoerId()) : null))
                            .tidspunkt(avbruttPlan.tidspunkt)
                    )
                    .orElse(null);

    private static Function<Oppfoelgingsdialog, RSGodkjentPlan> godkjentplan2rs = oppfoelgingsdialog ->
            oppfoelgingsdialog.godkjentPlan
                    .map(godkjentPlan -> new RSGodkjentPlan()
                            .avbruttPlan(mapNullable(godkjentPlan, avbruttplan2rs))
                            .deltMedNAV(godkjentPlan.deltMedNAV)
                            .deltMedNAVTidspunkt(godkjentPlan.deltMedNAVTidspunkt)
                            .deltMedFastlege(godkjentPlan.deltMedFastlege)
                            .deltMedFastlegeTidspunkt(godkjentPlan.deltMedFastlegeTidspunkt)
                            .dokumentUuid(godkjentPlan.dokumentUuid)
                            .gyldighetstidspunkt(map(godkjentPlan.gyldighetstidspunkt, gyldighetstidspunkt2rs))
                            .opprettetTidspunkt(godkjentPlan.opprettetTidspunkt)
                            .tvungenGodkjenning(godkjentPlan.tvungenGodkjenning)
                    )
                    .orElse(null);

    private static Function<Godkjenning, RSGodkjenning> godkjenning2rs = godkjenning ->
            new RSGodkjenning()
                    .beskrivelse(godkjenning.beskrivelse)
                    .godkjent(godkjenning.godkjent)
                    .delMedNav(godkjenning.delMedNav)
                    .godkjentAv(new RSPerson()
                            .fnr(aktoerService().hentFnrForAktoer(godkjenning.godkjentAvAktoerId))
                    )
                    .godkjenningsTidspunkt(godkjenning.godkjenningsTidspunkt)
                    .gyldighetstidspunkt(mapNullable(godkjenning.gyldighetstidspunkt, gyldighetstidspunkt2rs));

    private static Function<Kommentar, RSKommentar> kommentar2rs = kommentar ->
            new RSKommentar()
                    .id(kommentar.id)
                    .tekst(kommentar.tekst)
                    .opprettetAv(new RSPerson()
                            .fnr(aktoerService().hentFnrForAktoer(kommentar.opprettetAvAktoerId)))
                    .opprettetTidspunkt(kommentar.opprettetDato)
                    .sistEndretAv(new RSPerson().fnr(aktoerService().hentFnrForAktoer(kommentar.sistEndretAvAktoerId)))
                    .sistEndretDato(kommentar.sistEndretDato);

    private static Function<Tiltak, RSTiltak> tiltak2rs = tiltak ->
            new RSTiltak()
                    .tiltakId(tiltak.id)
                    .tiltaknavn(tiltak.navn)
                    .beskrivelse(tiltak.beskrivelse)
                    .fom(tiltak.fom)
                    .tom(tiltak.tom)
                    .status(tiltak.status)
                    .gjennomfoering(tiltak.gjennomfoering)
                    .beskrivelseIkkeAktuelt(tiltak.beskrivelseIkkeAktuelt)
                    .opprettetAv(new RSPerson()
                            .fnr(aktoerService().hentFnrForAktoer(tiltak.opprettetAvAktoerId)))
                    .opprettetDato(tiltak.opprettetDato)
                    .sistEndretAv(new RSPerson().fnr(aktoerService().hentFnrForAktoer(tiltak.sistEndretAvAktoerId)))
                    .sistEndretDato(tiltak.sistEndretDato)
                    .kommentarer(mapListe(tiltak.kommentarer, kommentar2rs));

    private static Function<Gjennomfoering, RSGjennomfoering> gjennomfoering2rs = gjennomfoering -> {
        if (KAN.name().equals(gjennomfoering.gjennomfoeringStatus)) {
            return new RSGjennomfoering().kanGjennomfoeres(KAN.name());
        } else if (KAN_IKKE.name().equals(gjennomfoering.gjennomfoeringStatus)) {
            return new RSGjennomfoering()
                    .kanGjennomfoeres(KAN_IKKE.name())
                    .kanIkkeBeskrivelse(gjennomfoering.kanIkkeBeskrivelse);
        } else if (TILRETTELEGGING.name().equals(gjennomfoering.gjennomfoeringStatus)) {
            return new RSGjennomfoering()
                    .kanGjennomfoeres(TILRETTELEGGING.name())
                    .kanBeskrivelse(gjennomfoering.kanBeskrivelse)
                    .paaAnnetSted(gjennomfoering.paaAnnetSted)
                    .medMerTid(gjennomfoering.medMerTid)
                    .medHjelp(gjennomfoering.medHjelp);
        } else {
            return null;
        }
    };

    private static Function<Arbeidsoppgave, RSArbeidsoppgave> arbeidsoppgave2rs = arbeidsoppgave ->
            new RSArbeidsoppgave()
                    .arbeidsoppgaveId(arbeidsoppgave.id)
                    .arbeidsoppgavenavn(arbeidsoppgave.navn)
                    .erVurdertAvSykmeldt(arbeidsoppgave.erVurdertAvSykmeldt)
                    .gjennomfoering(mapNullable(arbeidsoppgave.gjennomfoering, gjennomfoering2rs))
                    .opprettetAv(new RSPerson().fnr(aktoerService().hentFnrForAktoer(arbeidsoppgave.opprettetAvAktoerId)))
                    .opprettetDato(arbeidsoppgave.opprettetDato)
                    .sistEndretAv(new RSPerson().fnr(aktoerService().hentFnrForAktoer(arbeidsoppgave.sistEndretAvAktoerId)))
                    .sistEndretDato(arbeidsoppgave.sistEndretDato);

    private static Function<Oppfoelgingsdialog, RSEvaluering> evalueringSykmeldt2rs = oppfolgingsplan ->
            oppfolgingsplan.godkjentPlan
                    .map(godkjentPlan -> new RSEvaluering())
                    .orElse(null);

    private static Function<Oppfoelgingsdialog, RSEvaluering> evalueringArbeidsgiver2rs = oppfolgingsplan ->
            oppfolgingsplan.godkjentPlan
                    .map(godkjentPlan -> new RSEvaluering())
                    .orElse(null);

    public static Function<Oppfoelgingsdialog, RSBrukerOppfolgingsplan> oppfolgingsplan2rs = oppfolgingsplan -> new RSBrukerOppfolgingsplan()
            .id(oppfolgingsplan.id)
            .virksomhet(new RSVirksomhet().virksomhetsnummer(oppfolgingsplan.virksomhet.virksomhetsnummer))
            .arbeidsoppgaveListe(mapListe(oppfolgingsplan.arbeidsoppgaveListe, arbeidsoppgave2rs))
            .tiltakListe(mapListe(oppfolgingsplan.tiltakListe, tiltak2rs))
            .godkjenninger(mapListe(oppfolgingsplan.godkjenninger, godkjenning2rs))
            .sistEndretAv(new RSPerson().fnr(aktoerService().hentFnrForAktoer(oppfolgingsplan.sistEndretAvAktoerId)))
            .sistEndretDato(oppfolgingsplan.sistEndretDato)
            .godkjentPlan(mapNullable(oppfolgingsplan, godkjentplan2rs))
            .status(map(oppfolgingsplan, status2rs))
            .opprettetDato(LocalDate.from(oppfolgingsplan.opprettet))
            .arbeidstaker(new RSPerson()
                    .fnr(aktoerService().hentFnrForAktoer(oppfolgingsplan.arbeidstaker.aktoerId))
                    .sistInnlogget(oppfolgingsplan.arbeidstaker.sistInnlogget)
                    .evaluering(mapNullable(oppfolgingsplan, evalueringSykmeldt2rs))
                    .samtykke(oppfolgingsplan.arbeidstaker.samtykke)
            )
            .arbeidsgiver(new RSArbeidsgiver().naermesteLeder(new RSNaermesteLeder()
                    .sistInnlogget(oppfolgingsplan.arbeidsgiver.sistInnlogget)
                    .evaluering(mapNullable(oppfolgingsplan, evalueringArbeidsgiver2rs))
                    .samtykke(oppfolgingsplan.arbeidsgiver.samtykke))
            );
}
