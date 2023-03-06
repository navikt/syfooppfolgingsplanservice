package no.nav.syfo.api.v2.domain.oppfolgingsplan

import java.time.LocalDate

data class TiltakRequest(
    val tiltakId: Long? = null,
    val tiltaknavn: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val beskrivelse: String,
    val beskrivelseIkkeAktuelt: String? = null,
    val status: String,
    val gjennomfoering: String? = null,
)
