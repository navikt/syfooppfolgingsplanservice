package no.nav.syfo.lps.database

import no.nav.syfo.lps.OppfolgingsplanLPS
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
    val deltMedFastlege: Boolean,
    val archiveReference: String
)

fun POppfolgingsplanLPS.mapToOppfolgingsplanLPS(): OppfolgingsplanLPS {
    return OppfolgingsplanLPS(
        id = this.id,
        uuid = this.uuid,
        fnr = this.fnr,
        virksomhetsnummer = this.virksomhetsnummer,
        opprettet = this.opprettet,
        sistEndret = this.sistEndret,
        pdf = this.pdf,
        xml = this.xml,
        deltMedNav = this.deltMedNav,
        delMedFastlege = this.delMedFastlege,
        deltMedFastlege = this.deltMedFastlege,
        archiveReference = this.archiveReference
    )
}
