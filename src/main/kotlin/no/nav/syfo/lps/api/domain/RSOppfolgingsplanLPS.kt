package no.nav.syfo.lps.api.domain

import java.time.LocalDateTime
import java.util.*

data class RSOppfolgingsplanLPS(
    val uuid: UUID,
    val fnr: String,
    val virksomhetsnummer: String,
    val opprettet: LocalDateTime,
    val sistEndret: LocalDateTime
)
