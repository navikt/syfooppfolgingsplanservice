package no.nav.syfo.service

import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.model.Varseltype
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NarmesteLederVarselService(private val tredjepartsvarselService: TredjepartsvarselService, private val esyfovarselService: EsyfovarselService) {

    fun sendVarselTilNaermesteLeder(varseltype: Varseltype, narmesteleder: Naermesteleder) {
        // Sender varsel til nærmeste leder på sms/epost, denne skal etter hvert fjernes når esyfovarsel også sender varsel til arbeidsgivernotifikasjoner
        tredjepartsvarselService.sendVarselTilNaermesteLeder(varseltype, narmesteleder)
        // Sender varsel til Dine sykmeldte
        esyfovarselService.sendVarselTilNarmesteLeder(varseltype, narmesteleder)
    }
}