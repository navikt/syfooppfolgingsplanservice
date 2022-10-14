package no.nav.syfo.api.v2.domain.oppfolgingsplan

data class Evaluering(
    val effekt: String? = null,
    val hvorfor: String? = null,
    val videre: String? = null,
    val interneaktiviteter: Boolean = false,
    val ekstratid: Boolean? = null,
    val bistand: Boolean? = null,
    val ingen: Boolean? = null,
)
