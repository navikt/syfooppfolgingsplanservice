package no.nav.syfo.api.v3.domain.oppfolgingsplan

import java.time.LocalDateTime

data class AvbruttPlan(
    val av: Person,
    val tidspunkt: LocalDateTime,
    var id: Long? = null,
)
