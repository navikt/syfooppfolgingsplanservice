package no.nav.syfo.api.v3.mapper

import no.nav.syfo.api.v3.domain.oppfoelgingsdialog.RSGodkjentPlan
import no.nav.syfo.api.v3.domain.oppfoelgingsdialog.RSGyldighetstidspunkt
import no.nav.syfo.api.v3.domain.oppfoelgingsdialog.RSOppfoelgingsdialog
import no.nav.syfo.api.v3.domain.oppfoelgingsdialog.RSVirksomhet
import no.nav.syfo.domain.GodkjentPlan
import no.nav.syfo.domain.Oppfolgingsplan
import java.time.LocalDate

fun godkjentplan2rs(godkjentPlan: GodkjentPlan): RSGodkjentPlan {
    return RSGodkjentPlan(
        deltMedNAV = godkjentPlan.deltMedNAV,
        deltMedNAVTidspunkt = godkjentPlan.deltMedNAVTidspunkt,
        deltMedFastlege = godkjentPlan.deltMedFastlege,
        deltMedFastlegeTidspunkt = godkjentPlan.deltMedFastlegeTidspunkt,
        dokumentUuid = godkjentPlan.dokumentUuid,
        opprettetTidspunkt = godkjentPlan.opprettetTidspunkt,
        tvungenGodkjenning = godkjentPlan.tvungenGodkjenning,
        gyldighetstidspunkt = RSGyldighetstidspunkt(
            fom = godkjentPlan.gyldighetstidspunkt.fom,
            tom = godkjentPlan.gyldighetstidspunkt.tom,
            evalueres = godkjentPlan.gyldighetstidspunkt.evalueres
        )
    )
}

fun status2rs(oppfoelgingsdialog: Oppfolgingsplan): String {
    if (!oppfoelgingsdialog.godkjentPlan.isPresent) {
        return "UNDER_ARBEID"
    }

    return when {
        oppfoelgingsdialog.godkjentPlan.get().avbruttPlan.isPresent -> {
            "AVBRUTT"
        }

        oppfoelgingsdialog.godkjentPlan.get().gyldighetstidspunkt.tom.isBefore(LocalDate.now()) -> {
            "UTDATERT"
        }

        else -> "AKTIV"
    }
}


fun oppfoelgingsdialog2rs(oppfoelgingsdialog: Oppfolgingsplan): RSOppfoelgingsdialog {
    return RSOppfoelgingsdialog(
        id = oppfoelgingsdialog.id,
        uuid = oppfoelgingsdialog.uuid,
        sistEndretAvAktoerId = oppfoelgingsdialog.sistEndretAvAktoerId,
        sistEndretDato = oppfoelgingsdialog.sistEndretDato,
        status = status2rs(oppfoelgingsdialog),
        virksomhet = RSVirksomhet(
            virksomhetsnummer = oppfoelgingsdialog.virksomhet.virksomhetsnummer,
            navn = oppfoelgingsdialog.virksomhet.navn
        ),
        godkjentPlan = oppfoelgingsdialog.godkjentPlan.map { godkjentplan2rs(it) }.orElse(null),
    )
}
