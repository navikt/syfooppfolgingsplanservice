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
import no.nav.syfo.metric.Metrikk
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
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
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService,
    private val metrikk: Metrikk
) {
    private val log = LoggerFactory.getLogger(AltinnKanalKafkaConsumer::class.java)

    @KafkaListener(topics = ["aapen-altinn-oppfolgingsplan-Mottatt"])
    fun handleIncomingAltinnOP(
        consumerRecord: ConsumerRecord<String, ExternalAttachment>
    ) {
        try {
            log.info("KAFKA-TRACE(LPS): Mottatt melding aapen-altinn-oppfolgingsplan-Mottatt")

            val recordBatch = consumerRecord.value().getBatch()

            val dataBatch = dataBatchUnmarshaller.unmarshal(StringReader(recordBatch)) as DataBatch
            val payload = dataBatch.dataUnits.dataUnit.first().formTask.form.first().formData
            val oppfolgingsplan = xmlMapper.readValue<Oppfoelgingsplan4UtfyllendeInfoM>(payload)
            val skjemainnhold = oppfolgingsplan.skjemainnhold

            log.info("KAFKA-TRACE(LPS): Lagrer LPS-plan i DB with reference ${consumerRecord.value().getArchiveReference()}")
            val virksomhetsnummer = Virksomhetsnummer(skjemainnhold.arbeidsgiver.orgnr)

            oppfolgingsplanLPSService.receivePlan(
                consumerRecord.value().getArchiveReference(),
                recordBatch,
                skjemainnhold,
                virksomhetsnummer
            )
            metrikk.tellHendelse("prosessering_av_lps_plan_vellykket") }
        catch (e: Exception) {
            log.error("KAFKA-TRACE(LPS): Klarte ikke prosessere melding", e)
            metrikk.tellHendelse("prosessering_av_lps_plan_feilet")
        }
    }
}
