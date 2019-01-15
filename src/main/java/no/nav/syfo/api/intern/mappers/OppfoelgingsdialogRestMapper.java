package no.nav.syfo.api.intern.mappers;

import no.nav.syfo.api.intern.domain.RSGodkjentPlan;
import no.nav.syfo.api.intern.domain.RSGyldighetstidspunkt;
import no.nav.syfo.api.intern.domain.RSOppfoelgingsdialog;
import no.nav.syfo.api.intern.domain.RSVirksomhet;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfoelgingsdialog;

import java.time.LocalDate;
import java.util.function.Function;

import static no.nav.syfo.util.MapUtil.map;

public class OppfoelgingsdialogRestMapper {


    public static Function<GodkjentPlan, RSGodkjentPlan> godkjentplan2rs = godkjentPlan -> new RSGodkjentPlan()
            .deltMedNAV(godkjentPlan.deltMedNAV)
            .deltMedNAVTidspunkt(godkjentPlan.deltMedNAVTidspunkt)
            .deltMedFastlege(godkjentPlan.deltMedFastlege)
            .deltMedFastlegeTidspunkt(godkjentPlan.deltMedFastlegeTidspunkt)
            .dokumentUuid(godkjentPlan.dokumentUuid)
            .opprettetTidspunkt(godkjentPlan.opprettetTidspunkt)
            .tvungenGodkjenning(godkjentPlan.tvungenGodkjenning)
            .gyldighetstidspunkt(new RSGyldighetstidspunkt()
                    .fom(godkjentPlan.gyldighetstidspunkt.fom)
                    .tom(godkjentPlan.gyldighetstidspunkt.tom)
                    .evalueres(godkjentPlan.gyldighetstidspunkt.evalueres)
            );

    private static Function<Oppfoelgingsdialog, String> status2rs = oppfoelgingsdialog -> {
        if (oppfoelgingsdialog.godkjentPlan.isPresent()) {
            if (oppfoelgingsdialog.godkjentPlan.get().avbruttPlan.isPresent()) {
                return "AVBRUTT";
            }
            if (oppfoelgingsdialog.godkjentPlan.get().gyldighetstidspunkt.tom.isBefore(LocalDate.now())) {
                return "UTDATERT";
            }
            return "AKTIV";
        }
        return "UNDER_ARBEID";
    };


    public static Function<Oppfoelgingsdialog, RSOppfoelgingsdialog> oppfoelgingsdialog2rs = oppfoelgingsdialog ->
            new RSOppfoelgingsdialog()
                    .id(oppfoelgingsdialog.id)
                    .uuid(oppfoelgingsdialog.uuid)
                    .sistEndretAvAktoerId(oppfoelgingsdialog.sistEndretAvAktoerId)
                    .sistEndretDato(oppfoelgingsdialog.sistEndretDato)
                    .status(map(oppfoelgingsdialog, status2rs))
                    .virksomhet(new RSVirksomhet().virksomhetsnummer(oppfoelgingsdialog.virksomhet.virksomhetsnummer))
                    .godkjentPlan(map(oppfoelgingsdialog.godkjentPlan.get(), godkjentplan2rs));
}
