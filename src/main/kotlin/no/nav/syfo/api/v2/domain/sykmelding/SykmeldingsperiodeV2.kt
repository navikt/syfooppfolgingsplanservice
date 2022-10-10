package no.nav.syfo.api.v2.domain.sykmelding

import no.nav.syfo.model.Sykmeldingsperiode
import java.time.LocalDate

data class SykmeldingsperiodeV2(
    val fom: LocalDate,
    val tom: LocalDate
)

fun Sykmeldingsperiode.toSykmeldingsperiodeV2(): SykmeldingsperiodeV2 =
    SykmeldingsperiodeV2(
        fom = this.fom,
        tom = this.tom
    )
