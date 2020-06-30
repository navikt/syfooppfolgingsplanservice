package no.nav.syfo.testhelper

import no.nav.syfo.api.selvbetjening.domain.RSTiltak
import java.time.LocalDate

fun rsTiltakLagreNytt(): RSTiltak {
    return RSTiltak()
        .tiltaknavn("Tiltaknavn")
        .beskrivelse("Dette er en beskrivelse av et tiltak")
        .fom(LocalDate.now().plusDays(2))
        .tom(LocalDate.now().plusDays(4))
        .status("godkjent")
        .gjennomfoering("Dette er en gjennomføring av et tiltak")
        .knyttetTilArbeidsoppgaveId(1L)
        .gjennomfoering("Dette tiltaket kan følges opp ved å gjennomføres")
}

fun rsTiltakLagreEksisterende(): RSTiltak {
    return rsTiltakLagreNytt()
        .tiltakId(1L)
}