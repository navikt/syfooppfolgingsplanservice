package no.nav.syfo.api.v2.domain.oppfolgingsplan

data class Arbeidsgiver(
    val narmesteLeder: NarmesteLeder,
    val forrigeNarmesteLeder: NarmesteLeder? = null,
)
