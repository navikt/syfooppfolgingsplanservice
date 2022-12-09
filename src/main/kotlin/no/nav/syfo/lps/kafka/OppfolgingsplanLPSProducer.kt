package no.nav.syfo.lps.kafka

import no.nav.syfo.metric.Metrikk
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject

@Component
class OppfolgingsplanLPSProducer @Inject constructor(
    private val oppfolgingsplanLPSKafkaTemplate: KafkaTemplate<String, KOppfolgingsplanLPS>,
    private val metrikk: Metrikk
) {
    fun sendOppfolgingsLPSTilNAV(kOppfolgingsplanLPS: KOppfolgingsplanLPS) {
        try {
            oppfolgingsplanLPSKafkaTemplate.send(
                OPPFOLGINGSPLAN_LPS_TOPIC,
                UUID.randomUUID().toString(),
                kOppfolgingsplanLPS
            ).get()
            log.info("Sendt KOppfolgingsplanLPS til kø")
            tellLpsPlanDeltMedNav(true)
        } catch (e: Exception) {
            log.error("Feil ved sending av KOppfolgingsplanLPS til kø", e)
            tellLpsPlanDeltMedNav(false)
            throw RuntimeException("Feil ved sending av KOppfolgingsplanLPS til kø", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OppfolgingsplanLPSProducer::class.java)
        const val OPPFOLGINGSPLAN_LPS_TOPIC = "team-esyfo.aapen-syfo-oppfolgingsplan-lps-nav-v2"
    }

    fun tellLpsPlanDeltMedNav(delt: Boolean) {
        metrikk.tellHendelseMedTag("lps_plan_delt_med_nav", "delt", delt)
    }
}
