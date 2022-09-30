package no.nav.syfo.api.v2.domain.oppfolgingsplan

import java.math.BigDecimal
import java.time.LocalDate

data class Stilling(
    var virksomhetsnummer: String,
    var yrke: String,
    var prosent: BigDecimal,
    var fom: LocalDate,
    var tom: LocalDate,
)
