package no.nav.syfo.service

import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.model.Varseltype
import no.nav.syfo.model.Varseltype.SyfoplanOpprettetNL
import no.nav.syfo.model.Varseltype.SyfoplangodkjenningNl
import no.nav.syfo.varsling.EsyfovarselProducer
import no.nav.syfo.varsling.domain.EsyfovarselHendelse
import no.nav.syfo.varsling.domain.NarmesteLederVarselData
import no.nav.syfo.varsling.domain.HendelseType.NL_OPPFOLGINGSPLAN_OPPRETTET
import no.nav.syfo.varsling.domain.HendelseType.NL_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING
import no.nav.syfo.varsling.domain.HendelseType
import org.springframework.stereotype.Service

@Service
class EsyfovarselService (private val producer: EsyfovarselProducer) {

    fun sendVarselTilNarmesteLeder(varseltype: Varseltype, narmesteleder: Naermesteleder) {

        val esyfovarselHendelse = EsyfovarselHendelse(
            narmesteleder.naermesteLederFnr,
            getEsyfovarselHendelseType(varseltype),
            NarmesteLederVarselData(narmesteleder.ansattFnr, narmesteleder.orgnummer)
        )

        producer.sendVarselTilEsyfovarsel(esyfovarselHendelse)
    }

    private fun getEsyfovarselHendelseType(varseltype: Varseltype): HendelseType {
        return when (varseltype) {
            SyfoplanOpprettetNL -> NL_OPPFOLGINGSPLAN_OPPRETTET
            SyfoplangodkjenningNl -> NL_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING
            else -> throw RuntimeException("Not implemented")
        }
    }

}