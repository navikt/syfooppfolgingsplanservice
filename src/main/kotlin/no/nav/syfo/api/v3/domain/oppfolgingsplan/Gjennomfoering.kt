package no.nav.syfo.api.v3.domain.oppfolgingsplan

data class Gjennomfoering (
    val kanGjennomfoeres: String,
    val paaAnnetSted: Boolean? = null,
    val medMerTid: Boolean? = null,
    val medHjelp: Boolean? = null,
    val kanBeskrivelse: String? = null,
    val kanIkkeBeskrivelse: String? = null,
)
