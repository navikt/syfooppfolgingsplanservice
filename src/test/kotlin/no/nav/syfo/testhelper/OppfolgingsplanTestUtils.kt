package no.nav.syfo.testhelper

import no.nav.syfo.domain.*
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_AKTORID
import no.nav.syfo.testhelper.UserConstants.LEDER_AKTORID
import no.nav.syfo.testhelper.UserConstants.VIRKSOMHETSNUMMER
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

private fun arbeidstakeren(): Person {
    return Person()
        .aktoerId(ARBEIDSTAKER_AKTORID)
}

private fun arbeidsgiveren(): Person {
    return Person()
        .aktoerId(LEDER_AKTORID)
}

private fun oppfolgingsplanOpprettet(): Oppfolgingsplan {
    return Oppfolgingsplan()
        .id(1L)
        .status("UNDER_ARBEID")
        .opprettet(LocalDateTime.now().minusDays(7))
        .opprettetAvAktoerId(ARBEIDSTAKER_AKTORID)
        .sistEndretDato(LocalDateTime.now())
        .virksomhet(Virksomhet()
            .virksomhetsnummer(VIRKSOMHETSNUMMER)
        )
        .sistEndretAvAktoerId(LEDER_AKTORID)
        .arbeidstaker(arbeidstakeren())
        .arbeidsgiver(arbeidsgiveren())
}

fun oppfolgingsplanGodkjentTvang(): Oppfolgingsplan {
    return oppfolgingsplanOpprettet()
        .godkjentPlan(Optional.ofNullable(GodkjentPlan()
            .opprettetTidspunkt(LocalDateTime.now().minusDays(1))
            .tvungenGodkjenning(true)
            .gyldighetstidspunkt(Gyldighetstidspunkt()
                .fom(LocalDate.now().plusDays(3))
                .tom(LocalDate.now().plusDays(33))
                .evalueres(LocalDate.now().plusDays(40))
            )
            .dokumentUuid("DOKUMENTID")))
        .tiltakListe(listOf(
            Tiltak()
                .id(1L)
                .navn("Stå opp senere")
                .status("FORESLATT")
                .opprettetDato(LocalDateTime.now())
                .opprettetAvAktoerId(LEDER_AKTORID)
                .sistEndretDato(LocalDateTime.now())
                .sistEndretAvAktoerId(LEDER_AKTORID)
        ))
        .arbeidsoppgaveListe(listOf(
            Arbeidsoppgave()
                .id(1L)
                .navn("Mate grisene")
                .opprettetDato(LocalDateTime.now())
                .opprettetAvAktoerId(ARBEIDSTAKER_AKTORID)
                .sistEndretDato(LocalDateTime.now())
                .sistEndretAvAktoerId(ARBEIDSTAKER_AKTORID)
        ))
}
