package no.nav.syfo.lps.kafka

import java.time.LocalDateTime
import java.util.*

data class AltinnLpsOppfolgingsplan (
    val archiveReference: String,
    val uuid: UUID,
    val lpsFnr: String,
    val fnr: String?,
    val orgnummer: String,
    val pdf: ByteArray?,
    val xml: String,
    val shouldSendToNav: Boolean,
    val shouldSendToFastlege: Boolean,
    val sentToNav: Boolean,
    val sentToFastlege: Boolean,
    val sendToFastlegeRetryCount: Int,
    val journalpostId: String?,
    val created: LocalDateTime,
    val lastChanged: LocalDateTime,
)