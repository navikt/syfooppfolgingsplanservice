package no.nav.syfo.repository.domain

import java.time.LocalDateTime

data class PFeiletSending(
    var id: Long,
    var oppfolgingsplanId: Long,
    var number_of_tries: Int,
    var max_retries: Int,
    var sistEndretDato: LocalDateTime,
    val opprettetDato: LocalDateTime
)
