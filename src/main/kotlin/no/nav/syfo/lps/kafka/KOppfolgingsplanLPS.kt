package no.nav.syfo.lps.kafka

data class KOppfolgingsplanLPS(
    val uuid: String,
    val fodselsnummer: String,
    val virksomhetsnummer: String,
    val behovForBistandFraNav: Boolean,
    val opprettet: Int
)
