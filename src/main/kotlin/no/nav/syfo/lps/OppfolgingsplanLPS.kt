package no.nav.syfo.lps

import no.nav.syfo.lps.api.domain.RSOppfolgingsplanLPS
import java.time.LocalDateTime
import java.util.*

data class OppfolgingsplanLPS(
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
    val deltMedFastlege: Boolean,
    val archiveReference: String
)

fun OppfolgingsplanLPS.mapToRSOppfolgingsplanLPS(): RSOppfolgingsplanLPS {
    return RSOppfolgingsplanLPS(
        uuid = this.uuid,
        fnr = this.fnr,
        virksomhetsnummer = this.virksomhetsnummer,
        opprettet = this.opprettet,
        sistEndret = this.sistEndret
    )
}
