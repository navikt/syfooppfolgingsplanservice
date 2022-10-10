package no.nav.syfo.api.v2.domain.oppfolgingsplan

import java.time.LocalDateTime

data class Arbeidsoppgave (
    val arbeidsoppgaveId: Long,
    val arbeidsoppgavenavn: String,
    val erVurdertAvSykmeldt : Boolean,
    val gjennomfoering: Gjennomfoering?,
    val opprettetDato: LocalDateTime,
    val sistEndretDato: LocalDateTime,
    val sistEndretAv: Person,
    val opprettetAv: Person,
)
