package no.nav.syfo.varsling.domain

import java.io.Serializable

data class EsyfovarselHendelse(
    val mottakerFnr: String,
    val type: HendelseType,
    val data: EsyfovarselHendelseData
) : Serializable

enum class HendelseType {
    NL_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING,
    NL_OPPFOLGINGSPLAN_OPPRETTET
}

interface EsyfovarselHendelseData

data class NarmesteLederVarselData(
    val ansattFnr: String,
    val orgnummer: String
) : EsyfovarselHendelseData
