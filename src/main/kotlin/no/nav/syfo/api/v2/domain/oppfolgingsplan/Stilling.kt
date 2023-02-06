package no.nav.syfo.api.v2.domain.oppfolgingsplan

import java.math.BigDecimal

data class Stilling(
    var yrke: String,
    var prosent: BigDecimal,
)
