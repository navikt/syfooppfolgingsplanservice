package no.nav.syfo.api.v2.domain.oppfolgingsplan

import java.time.LocalDate

data class Gyldighetstidspunkt(
    val fom: LocalDate,
    val tom: LocalDate,
    val evalueres: LocalDate,
)
