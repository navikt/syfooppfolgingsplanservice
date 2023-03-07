package no.nav.syfo.api.v2.domain.oppfolgingsplan

data class ArbeidsoppgaveRequest (
    val arbeidsoppgaveId: Long? = null,
    val arbeidsoppgavenavn: String,
    val gjennomfoering: Gjennomfoering? = null,
)
