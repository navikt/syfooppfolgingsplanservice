package no.nav.syfo.api.v2.domain

data class Virksomhet(
    val virksomhetsnummer: String,
    val navn: String = ""
)
