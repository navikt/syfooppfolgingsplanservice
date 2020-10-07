package no.nav.syfo.testhelper

import no.nav.syfo.domain.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

private const val SYKMELDT_AKTOERID = "1010101010101"
private const val ARBEIDSGIVER_AKTOERID = "1010101010100"
const val VIRKSOMHETSNUMMER = "123456789"
private fun arbeidstakeren(): Person {
    return Person()
        .aktoerId(SYKMELDT_AKTOERID)
}

private fun arbeidsgiveren(): Person {
    return Person()
        .aktoerId(ARBEIDSGIVER_AKTOERID)
}

private fun oppfolgingsplanOpprettet(): Oppfolgingsplan {
    return Oppfolgingsplan()
        .id(1L)
        .status("UNDER_ARBEID")
        .opprettet(LocalDateTime.now().minusDays(7))
        .sistEndretDato(LocalDateTime.now())
        .virksomhet(Virksomhet()
            .virksomhetsnummer(VIRKSOMHETSNUMMER)
        )
        .sistEndretAvAktoerId(ARBEIDSGIVER_AKTOERID)
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
            )))
        .tiltakListe(listOf(
            Tiltak()
        ))
        .arbeidsoppgaveListe(listOf(
            Arbeidsoppgave()
        ))
}
