package no.nav.syfo.api.v3.domain.oppfoelgingsdialog

import java.time.LocalDateTime

data class RSOppfoelgingsdialog(
    var id: Long? = null,
    var uuid: String? = null,

    var sistEndretAvAktoerId: String? = null,
    var sistEndretDato: LocalDateTime? = null,

    var status: String? = null,
    var virksomhet: RSVirksomhet? = null,

    var godkjentPlan: RSGodkjentPlan? = null,

    var arbeidsgiver: RSPerson? = null,
    var arbeidstaker: RSPerson? = null
)
