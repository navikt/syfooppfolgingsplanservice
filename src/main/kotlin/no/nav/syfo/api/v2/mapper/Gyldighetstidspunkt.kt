package no.nav.syfo.api.v2.mapper

import no.nav.syfo.api.v2.domain.oppfolgingsplan.Gyldighetstidspunkt

fun Gyldighetstidspunkt.toGyldighetstidspunkt(): no.nav.syfo.domain.Gyldighetstidspunkt {
    val gyldighetstidspunkt = no.nav.syfo.domain.Gyldighetstidspunkt()
    gyldighetstidspunkt.fom = fom
    gyldighetstidspunkt.tom = tom
    gyldighetstidspunkt.evalueres = evalueres
    return gyldighetstidspunkt
}
