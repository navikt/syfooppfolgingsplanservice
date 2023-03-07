package no.nav.syfo.api.v2.mapper

import no.nav.syfo.api.v2.domain.oppfolgingsplan.Gjennomfoering
import no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.*

fun Gjennomfoering.toGjennomfoering(): no.nav.syfo.domain.Gjennomfoering {
    val gjennomfoering = no.nav.syfo.domain.Gjennomfoering()
    when (kanGjennomfoeres) {
        KAN.name -> {
            gjennomfoering.gjennomfoeringStatus = KAN.name
        }

        KAN_IKKE.name -> {
            gjennomfoering.gjennomfoeringStatus = KAN_IKKE.name
            gjennomfoering.kanIkkeBeskrivelse = kanIkkeBeskrivelse
        }

        TILRETTELEGGING.name -> {
            gjennomfoering.gjennomfoeringStatus = TILRETTELEGGING.name
            gjennomfoering.kanBeskrivelse = kanBeskrivelse
            gjennomfoering.paaAnnetSted = paaAnnetSted
            gjennomfoering.medMerTid = medMerTid
            gjennomfoering.medHjelp = medHjelp
        }
    }
    return gjennomfoering
}
