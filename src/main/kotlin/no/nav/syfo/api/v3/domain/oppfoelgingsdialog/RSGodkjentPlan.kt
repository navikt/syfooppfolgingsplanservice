package no.nav.syfo.api.v3.domain.oppfoelgingsdialog

import java.time.LocalDateTime

data class RSGodkjentPlan(
    var opprettetTidspunkt: LocalDateTime? = null,
    var gyldighetstidspunkt: RSGyldighetstidspunkt? = null,
    var tvungenGodkjenning: Boolean = false,
    var deltMedNAVTidspunkt: LocalDateTime? = null,
    var deltMedNAV: Boolean = false,
    var deltMedFastlegeTidspunkt: LocalDateTime? = null,
    var deltMedFastlege: Boolean = false,
    var dokumentUuid: String? = null
)
