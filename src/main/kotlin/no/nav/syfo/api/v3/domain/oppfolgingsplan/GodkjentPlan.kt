package no.nav.syfo.api.v3.domain.oppfolgingsplan

import java.time.LocalDateTime

data class GodkjentPlan(
    val opprettetTidspunkt: LocalDateTime,
    val gyldighetstidspunkt: Gyldighetstidspunkt,
    val tvungenGodkjenning: Boolean,
    val deltMedNAVTidspunkt: LocalDateTime,
    val deltMedNAV: Boolean,
    val deltMedFastlegeTidspunkt: LocalDateTime,
    val deltMedFastlege: Boolean,
    val dokumentUuid: String,
    val avbruttPlan: AvbruttPlan?,
)
