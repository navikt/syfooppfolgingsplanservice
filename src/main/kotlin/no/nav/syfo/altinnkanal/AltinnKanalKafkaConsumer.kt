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
import no.nav.syfo.lps.mq.EiaMottakProducer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.util.FnrUtil
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
    @Value("\${lps.eia.threshold.day}") private var thresholdDay: String,
    private val eiaMottakProducer: EiaMottakProducer,
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService,
    private val metrikk: Metrikk
) {
    private val log = LoggerFactory.getLogger(AltinnKanalKafkaConsumer::class.java)

    @KafkaListener(topics = ["aapen-altinn-oppfolgingsplan-Mottatt"])
    fun handleIncomingAltinnOP(
        consumerRecord: ConsumerRecord<String, ExternalAttachment>
    ) {
        try {
            log.info("Mottatt melding aapen-altinn-oppfolgingsplan-Mottatt")

            val recordBatch = consumerRecord.value().getBatch()

            val dataBatch = dataBatchUnmarshaller.unmarshal(StringReader(recordBatch)) as DataBatch
            val payload = dataBatch.dataUnits.dataUnit.first().formTask.form.first().formData
            val oppfolgingsplan = xmlMapper.readValue<Oppfoelgingsplan4UtfyllendeInfoM>(payload)
            val skjemainnhold = oppfolgingsplan.skjemainnhold
            val sykmeldtFnr = oppfolgingsplan.skjemainnhold.sykmeldtArbeidstaker.fnr
            val fnrDag = sykmeldtFnr.substring(0, 2).toInt()

            val skalSendeTilEia = FnrUtil.fodtEtterDagIMaaned(sykmeldtFnr, thresholdDay.toInt())

            log.info("Fnr dato/dag: $fnrDag")
            if (skalSendeTilEia) {
                log.info("Sender plan til EIA")
                eiaMottakProducer.sendOppfolgingsplanLPS(
                    consumerRecord.value(),
                    payload,
                    skjemainnhold.arbeidsgiver.orgnr
                )
                metrikk.tellHendelse("sendt_lps_plan_til_eia")
            } else {
                log.info("Lagrer plan i DB")
                val virksomhetsnummer = Virksomhetsnummer(skjemainnhold.arbeidsgiver.orgnr)
                oppfolgingsplanLPSService.receivePlan(
                    consumerRecord.value().getArchiveReference(),
                    recordBatch,
                    skjemainnhold,
                    virksomhetsnummer
                )
                metrikk.tellHendelse("lagret_lps_plan")
            }
            metrikk.tellHendelse("prosessering_av_lps_plan_vellykket") }
        catch (e: Exception) {
            log.error("KAFKA-TRACE: Klarte ikke prosessere melding", e)
            metrikk.tellHendelse("prosessering_av_lps_plan_feilet")
        }
    }
}
