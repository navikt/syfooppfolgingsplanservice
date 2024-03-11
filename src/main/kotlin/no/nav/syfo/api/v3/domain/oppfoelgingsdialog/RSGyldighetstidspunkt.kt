package no.nav.syfo.api.v3.domain.oppfoelgingsdialog

import java.time.LocalDate

data class RSGyldighetstidspunkt(
    var fom: LocalDate? = null,
    var tom: LocalDate? = null,
    var evalueres: LocalDate? = null
)
