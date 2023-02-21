package no.nav.syfo.service

import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.model.Varseltype
import no.nav.syfo.model.Varseltype.SyfoplangodkjenningNl
import no.nav.syfo.model.Varseltype.SyfoplangodkjenningSyk
import no.nav.syfo.varsling.EsyfovarselProducer
import no.nav.syfo.varsling.domain.ArbeidstakerHendelse
import no.nav.syfo.varsling.domain.HendelseType
import no.nav.syfo.varsling.domain.HendelseType.NL_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING
import no.nav.syfo.varsling.domain.HendelseType.SM_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING
import no.nav.syfo.varsling.domain.NarmesteLederHendelse
import org.springframework.stereotype.Service

@Service
class EsyfovarselService(private val producer: EsyfovarselProducer) {

    fun sendVarselTilNarmesteLeder(varseltype: Varseltype, narmesteleder: Naermesteleder) {

        val esyfovarselHendelse = NarmesteLederHendelse(
            getEsyfovarselHendelseType(varseltype),
            null,
            narmesteleder.naermesteLederFnr,
            narmesteleder.ansattFnr,
            narmesteleder.orgnummer
        )

        producer.sendVarselTilEsyfovarsel(esyfovarselHendelse)
    }

    fun sendVarselTilArbeidstaker(
        varseltype: Varseltype, arbeidstakerFnr: String,
        orgnummer: String
    ) {

        val esyfovarselHendelse = ArbeidstakerHendelse(
            getEsyfovarselHendelseType(varseltype),
            null,
            arbeidstakerFnr,
            orgnummer
        )

        producer.sendVarselTilEsyfovarsel(esyfovarselHendelse)
    }

    private fun getEsyfovarselHendelseType(varseltype: Varseltype): HendelseType {
        return when (varseltype) {
            SyfoplangodkjenningNl -> NL_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING
            SyfoplangodkjenningSyk -> SM_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING
        }
    }

}