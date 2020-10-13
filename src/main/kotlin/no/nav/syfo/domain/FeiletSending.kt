package no.nav.syfo.domain

import java.time.LocalDateTime

data class FeiletSending(
        val id: Long,
        val oppfolgingsplanId: Long,
        val number_of_tries: Int = 1,
        val sist_endret: LocalDateTime = LocalDateTime.now(),
        val opprettet: LocalDateTime = LocalDateTime.now()
)
