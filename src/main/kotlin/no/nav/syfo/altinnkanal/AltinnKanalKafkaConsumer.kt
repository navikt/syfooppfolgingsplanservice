package no.nav.syfo.altinnkanal

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import generated.DataBatch
import no.nav.altinnkanal.avro.ExternalAttachment
import no.nav.helse.op2016.Oppfoelgingsplan4UtfyllendeInfoM
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.lps.OppfolgingsplanLPSService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.io.StringReader
import javax.inject.Inject
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller

val dataBatchJaxBContext: JAXBContext = JAXBContext.newInstance(DataBatch::class.java)
val dataBatchUnmarshaller: Unmarshaller = dataBatchJaxBContext.createUnmarshaller()

val xmlMapper: ObjectMapper = XmlMapper(JacksonXmlModule().apply {
    setDefaultUseWrapper(false)
}).registerModule(JaxbAnnotationModule())
    .registerKotlinModule()
    .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)

@Profile("remote")
@Component
class AltinnKanalKafkaConsumer @Inject constructor(
    @Value("\${nais.cluster.name}") private val naisClusterName: String,
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService
) {
    private val log = LoggerFactory.getLogger(AltinnKanalKafkaConsumer::class.java)

    private val isDev = naisClusterName == "dev-fss"

    @KafkaListener(topics = ["aapen-altinn-oppfolgingsplan-Mottatt"])
    fun handleIncomingAltinnOP(
        consumerRecord: ConsumerRecord<String, ExternalAttachment>
    ) {
        if (isDev) {
            try {
                log.info("Mottatt melding aapen-altinn-oppfolgingsplan-Mottatt");

                val dataBatch = dataBatchUnmarshaller.unmarshal(StringReader(consumerRecord.value().getBatch())) as DataBatch
                val payload = dataBatch.dataUnits.dataUnit.first().formTask.form.first().formData
                val oppfolgingsplan = xmlMapper.readValue<Oppfoelgingsplan4UtfyllendeInfoM>(payload)
                val skjemainnhold = oppfolgingsplan.skjemainnhold

                val virksomhetsnummer = Virksomhetsnummer(skjemainnhold.arbeidsgiver.orgnr)
                oppfolgingsplanLPSService.receivePlan(
                    consumerRecord.value().getArchiveReference(),
                    consumerRecord.value().getBatch(),
                    skjemainnhold,
                    virksomhetsnummer
                )
            } catch (e: Exception) {
                log.info("KAFKA-TRACE: Klarte ikke prosessere melding med offset ${e.message}")
            }
        } else {
            log.info("KAFKA-TRACE: Skipping record")
        }
    }
}
