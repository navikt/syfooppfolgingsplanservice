package no.nav.syfo.dkif

import java.io.Serializable

data class DigitalKontaktinfo (
    val kanVarsles: Boolean,
    val reservert: Boolean,
    val mobiltelefonnummer: String?,
    val epostadresse: String?
) : Serializable
