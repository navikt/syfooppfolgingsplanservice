package no.nav.syfo.api.v2.mapper

import no.nav.syfo.api.v2.domain.oppfolgingsplan.KommentarRequest
import no.nav.syfo.domain.Kommentar

fun KommentarRequest.toKommentar(): Kommentar {
    val kommentar = Kommentar()
    kommentar.id = id
    kommentar.tekst = tekst
    return kommentar
}
