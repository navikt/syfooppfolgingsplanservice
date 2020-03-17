package no.nav.syfo.dkif

import java.io.Serializable

data class DigitalKontaktinfoBolk (
        val feil: Map<String, Feil>? = null,
        val kontaktinfo: Map<String, DigitalKontaktinfo>? = null
)

data class DigitalKontaktinfo(
        val epostadresse: String? = null,
        val kanVarsles: Boolean,
        val reservert: Boolean? = null,
        val mobiltelefonnummer: String? = null,
        val personident: String
) : Serializable

data class Feil(
        val melding: String
)
