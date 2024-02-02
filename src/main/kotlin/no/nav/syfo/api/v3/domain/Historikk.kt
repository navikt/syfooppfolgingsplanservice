package no.nav.syfo.api.v3.domain

import java.time.LocalDateTime

data class Historikk(
    val opprettetAv: String? = null,
    val tekst: String? = null,
    val tidspunkt: LocalDateTime? = null
)
