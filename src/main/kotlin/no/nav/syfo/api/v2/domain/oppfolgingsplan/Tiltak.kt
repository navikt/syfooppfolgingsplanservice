package no.nav.syfo.api.v2.domain.oppfolgingsplan

import java.time.LocalDate
import java.time.LocalDateTime

data class Tiltak(
    val tiltakId: Long,
    val tiltaknavn: String,
    val knyttetTilArbeidsoppgaveId: Long? = null,
    val fom: LocalDate,
    val tom: LocalDate,
    val beskrivelse: String?,
    val beskrivelseIkkeAktuelt: String? = null,

    val opprettetDato: LocalDateTime,
    val sistEndretDato: LocalDateTime,

    val kommentarer: List<Kommentar> = ArrayList(),
    val status: String,
    val gjennomfoering: String,

    val opprettetAv: Person,
    val sistEndretAv: Person
)
