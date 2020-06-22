package no.nav.syfo.lps.database

import java.time.LocalDateTime
import java.util.*

data class POppfolgingsplanLPS(
    val id: Long,
    val uuid: UUID,
    val fnr: String,
    val virksomhetsnummer: String,
    val opprettet: LocalDateTime,
    val sistEndret: LocalDateTime,
    val pdf: ByteArray?,
    val xml: String,
    val deltMedNav: Boolean,
    val delMedFastlege: Boolean,
    val deltMedFastlege: Boolean
)
