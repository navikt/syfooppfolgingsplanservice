package no.nav.syfo.api.v2.domain.oppfolgingsplan

import java.time.LocalDateTime

data class AvbruttPlan(
    val tidspunkt: LocalDateTime,
    var id: Long? = null,
)
