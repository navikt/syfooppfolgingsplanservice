package no.nav.syfo.api.v3.domain.oppfolgingsplan

import no.nav.syfo.api.v2.domain.Virksomhet
import java.time.LocalDate
import java.time.LocalDateTime

data class BrukerOppfolgingsplan (
    val id: Long,
    val sistEndretDato: LocalDateTime,
    val opprettetDato: LocalDate,
    val status: Status,
    val virksomhet: Virksomhet,
    val godkjentPlan: GodkjentPlan?,
    val godkjenninger: List<Godkjenning> = ArrayList(),
    val arbeidsoppgaveListe: List<Arbeidsoppgave> = ArrayList(),
    val tiltakListe: List<Tiltak> = ArrayList(),
    var avbruttPlanListe: List<AvbruttPlan> = ArrayList(),
    val arbeidsgiver: Arbeidsgiver,
    val arbeidstaker: Person,
    val sistEndretAv: Person,
)
