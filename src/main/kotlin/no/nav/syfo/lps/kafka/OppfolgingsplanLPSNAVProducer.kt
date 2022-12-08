package no.nav.syfo.lps.kafka

import no.nav.syfo.metric.Metrikk
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject

@Component
class OppfolgingsplanLPSNAVProducer @Inject constructor(
    private val oppfolgingsplanLPSNAVkafkaTemplate: KafkaTemplate<String, KOppfolgingsplanLPSNAV>,
    private val metrikk: Metrikk
) {
    fun sendOppfolgingsLPSTilNAV(kOppfolgingsplanLPSNAV: KOppfolgingsplanLPSNAV) {
        try {
            oppfolgingsplanLPSNAVkafkaTemplate.send(
                OPPFOLGINGSPLAN_LPS_NAV_TOPIC,
                UUID.randomUUID().toString(),
                kOppfolgingsplanLPSNAV
            ).get()
            log.info("Sendt KOppfolgingsplanLPSNAV til kø")
            tellLpsPlanDeltMedNav(true)
        } catch (e: Exception) {
            log.error("Feil ved sending av KOppfolgingsplanLPSNAV til kø", e)
            tellLpsPlanDeltMedNav(false)
            throw RuntimeException("Feil ved sending av KOppfolgingsplanLPSNAV til kø", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OppfolgingsplanLPSNAVProducer::class.java)
        const val OPPFOLGINGSPLAN_LPS_NAV_TOPIC = "team-esyfo.aapen-syfo-oppfolgingsplan-lps-nav-v2"
    }

    fun tellLpsPlanDeltMedNav(delt: Boolean) {
        metrikk.tellHendelseMedTag("lps_plan_delt_med_nav", "delt", delt)
    }
}
