package no.nav.syfo.api.v2.mapper

import no.nav.syfo.api.v2.domain.oppfolgingsplan.TiltakRequest
import no.nav.syfo.domain.Tiltak

fun TiltakRequest.toTiltak(): Tiltak {
    val tiltak = Tiltak()
    tiltak.id = tiltakId
    tiltak.beskrivelse = beskrivelse
    tiltak.beskrivelseIkkeAktuelt = beskrivelseIkkeAktuelt
    tiltak.fom = fom
    tiltak.tom = tom
    tiltak.navn = tiltaknavn
    tiltak.gjennomfoering = gjennomfoering
    tiltak.status = status
    return tiltak
}
