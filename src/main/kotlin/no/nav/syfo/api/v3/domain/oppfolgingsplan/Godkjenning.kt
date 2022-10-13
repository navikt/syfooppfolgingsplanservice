package no.nav.syfo.api.v3.domain.oppfolgingsplan

import java.time.LocalDateTime

data class Godkjenning(
    val godkjent: Boolean,
    val godkjentAv: Person,
    val beskrivelse: String,
    val godkjenningsTidspunkt: LocalDateTime,
    val gyldighetstidspunkt: Gyldighetstidspunkt,
    val delMedNav: Boolean,
)
