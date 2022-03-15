package no.nav.syfo.api.v2.domain

data class Kontaktinfo(
    val fnr: String,
    val epost: String? = null,
    val tlf: String? = null,
    val skalHaVarsel: Boolean,
    val feilAarsak: FeilAarsak? = null
)

enum class FeilAarsak {
    RESERVERT, UTGAATT, KONTAKTINFO_IKKE_FUNNET, SIKKERHETSBEGRENSNING, PERSON_IKKE_FUNNET
}
