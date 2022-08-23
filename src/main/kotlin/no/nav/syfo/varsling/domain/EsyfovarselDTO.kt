package no.nav.syfo.varsling.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
sealed interface EsyfovarselHendelse : Serializable {
    val mottakerFnr: String
    val type: HendelseType
    var data: Any?
}

data class NarmesteLederHendelse(
    override val mottakerFnr: String, // To be removed
    override val type: HendelseType,
    override var data: Any?,
    val narmesteLederFnr: String,
    val arbeidstakerFnr: String,
    val orgnummer: String
) : EsyfovarselHendelse

data class ArbeidstakerHendelse(
    override val mottakerFnr: String, // To be removed
    override val type: HendelseType,
    override var data: Any?,
    val arbeidstakerFnr: String,
    val orgnummer: String?
) : EsyfovarselHendelse

enum class HendelseType {
    NL_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING,
    NL_OPPFOLGINGSPLAN_OPPRETTET
}

interface EsyfovarselHendelseData : Serializable // To be removed

data class NarmesteLederVarselData( // To be removed
    val ansattFnr: String,
    val orgnummer: String
) : EsyfovarselHendelseData
