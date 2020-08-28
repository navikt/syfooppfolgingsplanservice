package no.nav.syfo.lps.kafka

import no.nav.syfo.oppfolgingsplan.avro.KOppfolgingsplanLPSNAV
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject

@Component
class OppfolgingsplanLPSNAVProducer @Inject constructor(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    fun sendOppfolgingsLPSTilNAV(kOppfolgingsplanLPSNAV: KOppfolgingsplanLPSNAV) {
        try {
            kafkaTemplate.send(
                OPPFOLGINGSPLAN_LPS_NAV_TOPIC,
                UUID.randomUUID().toString(),
                kOppfolgingsplanLPSNAV
            ).get()
            log.info("Sendt KOppfolgingsplanLPSNAV til kø")
        } catch (e: Exception) {
            log.error("Feil ved sending av KOppfolgingsplanLPSNAV til kø", e)
            throw RuntimeException("Feil ved sending av KOppfolgingsplanLPSNAV til kø", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OppfolgingsplanLPSNAVProducer::class.java)
        const val OPPFOLGINGSPLAN_LPS_NAV_TOPIC = "aapen-syfo-oppfolgingsplan-lps-nav-v1"
    }
}
