package no.nav.syfo.lps.database

import java.time.LocalDateTime

data class POppfolgingsplanLPSRetry(
    val id: Long,
    val fnr: String,
    val virksomhetsnummer: String,
    val xml: String,
    val opprettet: LocalDateTime
)
