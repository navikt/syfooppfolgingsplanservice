package no.nav.syfo.altinnkanal

import no.nav.altinnkanal.avro.ReceivedMessage
import no.nav.syfo.lps.OppfolgingsplanLPSService
import no.nav.syfo.metric.Metrikk
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import javax.inject.Inject

@Profile("remote")
@Component
class AltinnKanalKafkaConsumer @Inject constructor(
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService,
    private val metrikk: Metrikk
) {
    private val log = LoggerFactory.getLogger(AltinnKanalKafkaConsumer::class.java)

    @KafkaListener(topics = ["alf.aapen-altinn-oppfolgingsplan-mottatt-v2"])
    fun handleIncomingAltinnOP(
        consumerRecord: ConsumerRecord<String, ReceivedMessage>
    ) {
        try {
            log.info("KAFKA-TRACE(LPS): Mottatt melding alf.aapen-altinn-oppfolgingsplan-mottatt-v2")
            val receivedMessage = consumerRecord.value()
            val archiveReference = receivedMessage.getArchiveReference()
            val payload = receivedMessage.getXmlMessage()
            oppfolgingsplanLPSService.receivePlan(
                archiveReference,
                payload,
                false
            )
        } catch (e: Exception) {
            log.error("KAFKA-TRACE(LPS): Klarte ikke prosessere melding", e)
            metrikk.tellHendelse("prosessering_av_lps_plan_feilet")
        }
    }
}
