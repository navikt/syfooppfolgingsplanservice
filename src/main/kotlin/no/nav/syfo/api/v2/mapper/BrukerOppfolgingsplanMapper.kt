package no.nav.syfo.api.v2.mapper

import no.nav.syfo.api.util.unwrap
import no.nav.syfo.api.v2.domain.Virksomhet
import no.nav.syfo.api.v2.domain.oppfolgingsplan.*
import no.nav.syfo.api.v2.domain.oppfolgingsplan.Person
import no.nav.syfo.api.v2.domain.oppfolgingsplan.Status.*
import no.nav.syfo.domain.*
import no.nav.syfo.domain.Arbeidsoppgave
import no.nav.syfo.domain.Gjennomfoering
import no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.*
import no.nav.syfo.domain.Godkjenning
import no.nav.syfo.domain.GodkjentPlan
import no.nav.syfo.domain.Gyldighetstidspunkt
import no.nav.syfo.domain.Kommentar
import no.nav.syfo.domain.Tiltak
import no.nav.syfo.pdl.PdlConsumer
import java.time.LocalDate

fun Oppfolgingsplan.toBrukerOppfolgingsplan(pdlConsumer: PdlConsumer) =
    BrukerOppfolgingsplan(
        id = id,
        virksomhet = Virksomhet(virksomhet.virksomhetsnummer),
        arbeidsoppgaveListe = arbeidsoppgaveListe.map { it.toArbeidsoppgave(pdlConsumer) },
        tiltakListe = tiltakListe.map { it.toTiltak(pdlConsumer) },
        godkjenninger = godkjenninger.map { it.toGodkjenning(pdlConsumer) },
        sistEndretAv = Person(fnr = pdlConsumer.fnr(sistEndretAvAktoerId)),
        sistEndretDato = sistEndretDato,
        godkjentPlan = godkjentPlan.unwrap()?.toGodkjentPlan(),
        status = getStatus(),
        opprettetDato = LocalDate.from(opprettet),
        arbeidstaker = toArbeidstaker(pdlConsumer),
        arbeidsgiver = toArbeidsgiver()
    )

fun Oppfolgingsplan.toArbeidstaker(pdlConsumer: PdlConsumer) =
    Person(
        fnr = pdlConsumer.fnr(arbeidstaker.aktoerId),
        sistInnlogget = arbeidstaker.sistInnlogget,
        evaluering = if (godkjentPlan != null) Evaluering() else null,
        samtykke = arbeidstaker.samtykke
    )

fun Oppfolgingsplan.toArbeidsgiver() =
    Arbeidsgiver(
        narmesteLeder = NarmesteLeder(
            sistInnlogget = arbeidsgiver.sistInnlogget,
            evaluering = if (godkjentPlan != null) Evaluering() else null,
            samtykke = arbeidsgiver.samtykke
        )
    )

fun Oppfolgingsplan.getStatus(): Status {
    if (godkjentPlan.isPresent) {
        if (godkjentPlan.get().avbruttPlan.isPresent) {
            return AVBRUTT
        }
        if (godkjentPlan.get().gyldighetstidspunkt.tom.isBefore(LocalDate.now())) {
            return UTDATERT
        }
        return AKTIV
    }
    return UNDER_ARBEID
}

fun GodkjentPlan.toGodkjentPlan() =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.GodkjentPlan(
        avbruttPlan = avbruttPlan.unwrap()?.toAvbruttPlan(),
        deltMedNAV = deltMedNAV,
        deltMedNAVTidspunkt = deltMedNAVTidspunkt,
        deltMedFastlege = deltMedFastlege,
        deltMedFastlegeTidspunkt = deltMedFastlegeTidspunkt,
        dokumentUuid = dokumentUuid,
        gyldighetstidspunkt = gyldighetstidspunkt.toGyldighetstidspunkt(),
        opprettetTidspunkt = opprettetTidspunkt,
        tvungenGodkjenning = tvungenGodkjenning
    )

fun Avbruttplan.toAvbruttPlan() =
    AvbruttPlan(
        tidspunkt = tidspunkt
    )

fun Godkjenning.toGodkjenning(pdlConsumer: PdlConsumer) =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.Godkjenning(
        beskrivelse = beskrivelse,
        godkjent = godkjent,
        delMedNav = delMedNav,
        godkjentAv = Person(fnr = pdlConsumer.fnr(godkjentAvAktoerId)),
        godkjenningsTidspunkt = godkjenningsTidspunkt,
        gyldighetstidspunkt = gyldighetstidspunkt.toGyldighetstidspunkt()
    )

fun Gyldighetstidspunkt.toGyldighetstidspunkt() =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.Gyldighetstidspunkt(
        fom = fom,
        tom = tom,
        evalueres = evalueres
    )

fun Tiltak.toTiltak(pdlConsumer: PdlConsumer) =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.Tiltak(
        tiltakId = id,
        tiltaknavn = navn,
        beskrivelse = beskrivelse,
        fom = fom,
        tom = tom,
        status = status,
        gjennomfoering = gjennomfoering,
        beskrivelseIkkeAktuelt = beskrivelseIkkeAktuelt,
        opprettetAv = Person(fnr = pdlConsumer.fnr(opprettetAvAktoerId)),
        opprettetDato = opprettetDato,
        sistEndretAv = Person(fnr = pdlConsumer.fnr(sistEndretAvAktoerId)),
        sistEndretDato = sistEndretDato,
        kommentarer = kommentarer.map { it.toKommentar(pdlConsumer) }
    )

fun Kommentar.toKommentar(pdlConsumer: PdlConsumer) =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.Kommentar(
        id = id,
        tekst = tekst,
        opprettetAv = Person(fnr = pdlConsumer.fnr(opprettetAvAktoerId)),
        opprettetTidspunkt = opprettetDato,
        sistEndretAv = Person(fnr = pdlConsumer.fnr(sistEndretAvAktoerId)),
        sistEndretDato = sistEndretDato
    )

fun Arbeidsoppgave.toArbeidsoppgave(pdlConsumer: PdlConsumer) =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.Arbeidsoppgave(
        arbeidsoppgaveId = id,
        arbeidsoppgavenavn = navn,
        erVurdertAvSykmeldt = erVurdertAvSykmeldt,
        gjennomfoering = gjennomfoering.toGjennomfoering(),
        opprettetAv = Person(fnr = pdlConsumer.fnr(opprettetAvAktoerId)),
        opprettetDato = opprettetDato,
        sistEndretAv = Person(fnr = pdlConsumer.fnr(sistEndretAvAktoerId)),
        sistEndretDato = sistEndretDato
    )

fun Gjennomfoering.toGjennomfoering() =
    when (gjennomfoeringStatus) {
        KAN.name -> no.nav.syfo.api.v2.domain.oppfolgingsplan.Gjennomfoering(kanGjennomfoeres = KAN.name)
        KAN_IKKE.name -> no.nav.syfo.api.v2.domain.oppfolgingsplan.Gjennomfoering(
            kanGjennomfoeres = KAN_IKKE.name,
            kanIkkeBeskrivelse = kanIkkeBeskrivelse
        )

        TILRETTELEGGING.name -> no.nav.syfo.api.v2.domain.oppfolgingsplan.Gjennomfoering(
            kanGjennomfoeres = TILRETTELEGGING.name,
            kanBeskrivelse = kanBeskrivelse,
            paaAnnetSted = paaAnnetSted,
            medMerTid = medMerTid,
            medHjelp = medHjelp
        )

        else -> {
            null
        }
    }

fun BrukerOppfolgingsplan.populerPlanerMedAvbruttPlanListe(planer: List<BrukerOppfolgingsplan>) {
    avbruttPlanListe = planer.filter {
        it.arbeidstaker.fnr == arbeidstaker.fnr &&
                it.virksomhet.virksomhetsnummer == virksomhet.virksomhetsnummer &&
                it.godkjentPlan != null &&
                it.godkjentPlan.avbruttPlan != null &&
                it.opprettetDato.isBefore(opprettetDato)
    }
        .sortedByDescending { brukerOppfolgingsplan -> brukerOppfolgingsplan.godkjentPlan?.avbruttPlan?.tidspunkt }
        .map {
            it.godkjentPlan!!.avbruttPlan!!.id = it.id
            it.godkjentPlan.avbruttPlan!!
        }
}

fun BrukerOppfolgingsplan.populerArbeidstakersStillinger(arbeidsforhold: List<no.nav.syfo.model.Stilling>) {
    arbeidstaker.stillinger = arbeidsforhold
        .filter { stilling -> stilling.fom.isBefore(opprettetDato) && (stilling.tom == null || stilling.tom.isAfter(opprettetDato)) }
        .filter { stilling -> stilling.orgnummer.equals(virksomhet.virksomhetsnummer) }
        .map { stilling -> Stilling(stilling.yrke, stilling.prosent) }
}
