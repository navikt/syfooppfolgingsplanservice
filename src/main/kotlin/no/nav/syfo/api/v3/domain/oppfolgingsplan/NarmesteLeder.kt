package no.nav.syfo.api.v3.domain.oppfolgingsplan

import java.time.LocalDate
import java.time.LocalDateTime

data class NarmesteLeder(
    val virksomhetsnummer: String? = null,
    val erAktiv: Boolean? = null,
    val aktivFom: LocalDate? = null,
    val aktivTom: LocalDate? = null,
    val navn: String = "",
    val fnr: String? = null,
    val epost: String? = null,
    val tlf: String? = null,
    val sistInnlogget: LocalDateTime? = null,
    val samtykke: Boolean? = null,
    val evaluering: Evaluering? = null,
)
