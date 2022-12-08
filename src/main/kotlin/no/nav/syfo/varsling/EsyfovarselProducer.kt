package no.nav.syfo.varsling

import no.nav.syfo.varsling.domain.EsyfovarselHendelse
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class EsyfovarselProducer @Autowired constructor(
    private val esyfovarselKafkaTemplate: KafkaTemplate<String, EsyfovarselHendelse>,
) {
    fun sendVarselTilEsyfovarsel(
        esyfovarselHendelse: EsyfovarselHendelse,
    ) {
        try {
            esyfovarselKafkaTemplate.send(
                ProducerRecord(
                    ESYFOVARSEL_TOPIC,
                    UUID.randomUUID().toString(),
                    esyfovarselHendelse,
                )
            ).get()
        } catch (e: Exception) {
            log.error("Exception was thrown when attempting to send varsel to esyfovarsel. ${e.message}")
            throw e
        }
    }

    companion object {
        private const val ESYFOVARSEL_TOPIC = "team-esyfo.varselbus"
        private val log = LoggerFactory.getLogger(EsyfovarselProducer::class.java)
    }
}
