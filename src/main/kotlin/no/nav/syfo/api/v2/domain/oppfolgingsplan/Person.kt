package no.nav.syfo.api.v2.domain.oppfolgingsplan

import java.time.LocalDateTime

data class Person(
    val navn: String = " ",
    val fnr: String,
    val epost: String? = null,
    val tlf: String? = null,
    val sistInnlogget: LocalDateTime? = null,
    val samtykke: Boolean? = null,
    val evaluering: Evaluering? = null,
    var stillinger: List<Stilling> = ArrayList(),
)
