package no.nav.syfo.api.v3.domain

import no.nav.syfo.model.Stilling
import java.math.BigDecimal
import java.time.LocalDate

data class  Arbeidsforhold (
    val yrke: String,
    val prosent: BigDecimal,
    val fom: LocalDate,
    val tom: LocalDate
)

fun Stilling.mapToArbeidsforhold(): Arbeidsforhold {
    return Arbeidsforhold(
        yrke = this.yrke,
        prosent = this.prosent,
        fom = this.fom,
        tom = this.tom,
    )
}
