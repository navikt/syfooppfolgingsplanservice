package no.nav.syfo.lps

data class IncomingMetadata(
    val archiveReference: String,
    val senderOrgId: String,
    val senderOrgName: String,
    val userPersonNumber: String
)
