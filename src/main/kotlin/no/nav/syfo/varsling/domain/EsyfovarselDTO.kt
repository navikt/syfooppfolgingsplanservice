package no.nav.syfo.varsling.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
sealed interface EsyfovarselHendelse : Serializable {
    val type: HendelseType
    var data: Any?
}

data class NarmesteLederHendelse(
    override val type: HendelseType,
    override var data: Any?,
    val narmesteLederFnr: String,
    val arbeidstakerFnr: String,
    val orgnummer: String
) : EsyfovarselHendelse

data class ArbeidstakerHendelse(
    override val type: HendelseType,
    override var data: Any?,
    val arbeidstakerFnr: String,
    val orgnummer: String?
) : EsyfovarselHendelse

data class VarselData(
    val status: VarselStatus? = null,
    val uuid: VarselUUID? = null
)
data class VarselStatus(
    val ferdigstilt: Boolean = false
)

data class VarselUUID(
    val uuid: String
)

enum class HendelseType {
    NL_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING,
    SM_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING
}
