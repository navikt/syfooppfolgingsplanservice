package no.nav.syfo.api.v2.mapper

import no.nav.syfo.aktorregister.AktorregisterConsumer.aktorregisterConsumer
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
import java.time.LocalDate

fun Oppfolgingsplan.toBrukerOppfolgingsplan() =
    BrukerOppfolgingsplan(
        id = id,
        virksomhet = Virksomhet(virksomhet.virksomhetsnummer),
        arbeidsoppgaveListe = arbeidsoppgaveListe.map { it.toArbeidsoppgave() },
        tiltakListe = tiltakListe.map { it.toTiltak() },
        godkjenninger = godkjenninger.map { it.toGodkjenning() },
        sistEndretAv = Person(fnr = aktorregisterConsumer().hentFnrForAktor(sistEndretAvAktoerId)),
        sistEndretDato = sistEndretDato,
        godkjentPlan = godkjentPlan.unwrap()?.toGodkjentPlan(),
        status = getStatus(),
        opprettetDato = LocalDate.from(opprettet),
        arbeidstaker = toArbeidstaker(),
        arbeidsgiver = toArbeidsgiver()
    )

fun Oppfolgingsplan.toArbeidstaker() =
    Person(
        fnr = aktorregisterConsumer().hentFnrForAktor(arbeidstaker.aktoerId),
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
        av = Person(fnr = avAktoerId?.let { aktorregisterConsumer().hentFnrForAktor(avAktoerId) }),
        tidspunkt = tidspunkt
    )

fun Godkjenning.toGodkjenning() =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.Godkjenning(
        beskrivelse = beskrivelse,
        godkjent = godkjent,
        delMedNav = delMedNav,
        godkjentAv = Person(fnr = aktorregisterConsumer().hentFnrForAktor(godkjentAvAktoerId)),
        godkjenningsTidspunkt = godkjenningsTidspunkt,
        gyldighetstidspunkt = gyldighetstidspunkt.toGyldighetstidspunkt()
    )

fun Gyldighetstidspunkt.toGyldighetstidspunkt() =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.Gyldighetstidspunkt(
        fom = fom,
        tom = tom,
        evalueres = evalueres
    )

fun Tiltak.toTiltak() =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.Tiltak(
        tiltakId = id,
        tiltaknavn = navn,
        beskrivelse = beskrivelse,
        fom = fom,
        tom = tom,
        status = status,
        gjennomfoering = gjennomfoering,
        beskrivelseIkkeAktuelt = beskrivelseIkkeAktuelt,
        opprettetAv = Person(fnr = aktorregisterConsumer().hentFnrForAktor(opprettetAvAktoerId)),
        opprettetDato = opprettetDato,
        sistEndretAv = Person(fnr = aktorregisterConsumer().hentFnrForAktor(sistEndretAvAktoerId)),
        sistEndretDato = sistEndretDato,
        kommentarer = kommentarer.map { it.toKommentar() }
    )

fun Kommentar.toKommentar() =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.Kommentar(
        id = id,
        tekst = tekst,
        opprettetAv = Person(fnr = aktorregisterConsumer().hentFnrForAktor(opprettetAvAktoerId)),
        opprettetTidspunkt = opprettetDato,
        sistEndretAv = Person(fnr = aktorregisterConsumer().hentFnrForAktor(sistEndretAvAktoerId)),
        sistEndretDato = sistEndretDato
    )

fun Arbeidsoppgave.toArbeidsoppgave() =
    no.nav.syfo.api.v2.domain.oppfolgingsplan.Arbeidsoppgave(
        arbeidsoppgaveId = id,
        arbeidsoppgavenavn = navn,
        erVurdertAvSykmeldt = erVurdertAvSykmeldt,
        gjennomfoering = gjennomfoering.toGjennomfoering(),
        opprettetAv = Person(fnr = aktorregisterConsumer().hentFnrForAktor(opprettetAvAktoerId)),
        opprettetDato = opprettetDato,
        sistEndretAv = Person(fnr = aktorregisterConsumer().hentFnrForAktor(sistEndretAvAktoerId)),
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
        .map {
            it.godkjentPlan!!.avbruttPlan!!.id = it.id
            it.godkjentPlan.avbruttPlan!!
        }
}
