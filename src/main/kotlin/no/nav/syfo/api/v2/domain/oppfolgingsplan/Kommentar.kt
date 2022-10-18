package no.nav.syfo.api.v2.domain.oppfolgingsplan

import java.time.LocalDateTime

data class Kommentar(
    val id: Long,
    val tekst: String?,
    val opprettetTidspunkt: LocalDateTime,
    val sistEndretDato: LocalDateTime,
    val opprettetAv: Person,
    val sistEndretAv: Person
)
