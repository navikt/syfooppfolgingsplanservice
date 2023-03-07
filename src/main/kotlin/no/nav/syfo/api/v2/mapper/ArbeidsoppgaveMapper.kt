package no.nav.syfo.api.v2.mapper

import no.nav.syfo.api.v2.domain.oppfolgingsplan.ArbeidsoppgaveRequest
import no.nav.syfo.domain.Arbeidsoppgave

fun ArbeidsoppgaveRequest.toArbeidsoppgave(): Arbeidsoppgave {
    val arbeidsoppgave = Arbeidsoppgave()
    arbeidsoppgave.id=arbeidsoppgaveId
    arbeidsoppgave.navn=arbeidsoppgavenavn
    arbeidsoppgave.gjennomfoering=gjennomfoering?.toGjennomfoering()
    return arbeidsoppgave
}
