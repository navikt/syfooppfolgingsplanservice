package no.nav.syfo.service

import no.nav.syfo.api.v2.domain.GodkjennPlanVarsel
import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.model.Varseltype
import no.nav.syfo.model.Varseltype.SyfoplangodkjenningNl
import no.nav.syfo.model.Varseltype.SyfoplangodkjenningSyk
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.repository.dao.OppfolgingsplanDAO
import no.nav.syfo.varsling.EsyfovarselProducer
import no.nav.syfo.varsling.domain.*
import no.nav.syfo.varsling.domain.HendelseType.NL_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING
import no.nav.syfo.varsling.domain.HendelseType.SM_OPPFOLGINGSPLAN_SENDT_TIL_GODKJENNING
import org.springframework.stereotype.Service

@Service
class EsyfovarselService(
    private val producer: EsyfovarselProducer,
    private val narmesteLederConsumer: NarmesteLederConsumer,
    private val tilgangskontrollService: TilgangskontrollService,
    private val oppfolgingsplanDAO: OppfolgingsplanDAO
) {
    fun sendVarselTilNarmesteLeder(
        varseltype: Varseltype,
        narmesteleder: Naermesteleder
    ) {

        val esyfovarselHendelse = NarmesteLederHendelse(
            getEsyfovarselHendelseType(varseltype),
            VarselData(
                status = VarselStatus(ferdigstilt = false)
            ),
            narmesteleder.naermesteLederFnr,
            narmesteleder.ansattFnr,
            narmesteleder.orgnummer
        )

        producer.sendVarselTilEsyfovarsel(esyfovarselHendelse)
    }

    fun sendVarselTilArbeidstaker(
        varseltype: Varseltype,
        narmesteleder: Naermesteleder
    ) {
        val esyfovarselHendelse = ArbeidstakerHendelse(
            getEsyfovarselHendelseType(varseltype),
            VarselData(
                status = VarselStatus(ferdigstilt = false)
            ),
            narmesteleder.ansattFnr,
            narmesteleder.orgnummer
        )

        producer.sendVarselTilEsyfovarsel(esyfovarselHendelse)
    }

    fun ferdigstillVarsel(
        innloggetFnr: String,
        godkjennPlanVarsel: GodkjennPlanVarsel
    ) {
        val oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(godkjennPlanVarsel.oppfolgingsplanId)
        if (!tilgangskontrollService.brukerTilhorerOppfolgingsplan(innloggetFnr, oppfolgingsplan)) {
            throw IllegalArgumentException("Bruker forsøker å ferdigstille varsel for plan som ikke tilhørerer vedkommende")
        }

        val arbeidstakerFnr = oppfolgingsplan.arbeidstaker.fnr
        val virksomhetsnummer = oppfolgingsplan.virksomhet.virksomhetsnummer
        val narmesteleder = narmesteLederConsumer.narmesteLeder(arbeidstakerFnr, virksomhetsnummer).get()
        val erSykmeldt = godkjennPlanVarsel.erSykmeldt

        if (erSykmeldt) {
            ferdigstillVarselArbeidstaker(
                SyfoplangodkjenningSyk,
                narmesteleder
            )
        } else {
            ferdigstillVarselNarmesteLeder(
                SyfoplangodkjenningNl,
                narmesteleder
            )
        }
    }

    private fun ferdigstillVarselArbeidstaker(
        varseltype: Varseltype,
        narmesteleder: Naermesteleder
    ) {
        val esyfovarselHendelse = ArbeidstakerHendelse(
            getEsyfovarselHendelseType(varseltype),
            VarselData(
                status = VarselStatus(ferdigstilt = true)
            ),
            narmesteleder.ansattFnr,
            narmesteleder.orgnummer
        )
        producer.sendVarselTilEsyfovarsel(esyfovarselHendelse)
    }

    private fun ferdigstillVarselNarmesteLeder(
        varseltype: Varseltype,
        narmesteleder: Naermesteleder
    ) {
        val esyfovarselHendelse = NarmesteLederHendelse(
            getEsyfovarselHendelseType(varseltype),
            VarselData(
                status = VarselStatus(ferdigstilt = true)
            ),
            narmesteleder.naermesteLederFnr,
            narmesteleder.ansattFnr,
            narmesteleder.orgnummer
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
