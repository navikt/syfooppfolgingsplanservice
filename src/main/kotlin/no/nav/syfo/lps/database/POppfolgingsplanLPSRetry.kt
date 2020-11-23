package no.nav.syfo.lps.database

import java.time.LocalDateTime

data class POppfolgingsplanLPSRetry(
    val id: Long,
    val archiveReference: String,
    val xml: String,
    val opprettet: LocalDateTime
)
