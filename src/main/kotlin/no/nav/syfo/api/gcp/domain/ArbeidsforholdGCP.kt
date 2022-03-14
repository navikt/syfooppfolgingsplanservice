package no.nav.syfo.api.gcp.domain

import no.nav.syfo.model.Stilling
import java.math.BigDecimal

data class ArbeidsforholdGCP (
    val yrke: String,
    val prosent: BigDecimal
)

fun Stilling.mapToArbeidsforhold(): ArbeidsforholdGCP {
    return ArbeidsforholdGCP(
        yrke = this.yrke,
        prosent = this.prosent
    )
}