package no.nav.syfo.api.v3.domain

import no.nav.syfo.model.Stilling
import java.math.BigDecimal

data class Arbeidsforhold (
    val yrke: String,
    val prosent: BigDecimal
)

fun Stilling.mapToArbeidsforhold(): Arbeidsforhold {
    return Arbeidsforhold(
        yrke = this.yrke,
        prosent = this.prosent
    )
}
