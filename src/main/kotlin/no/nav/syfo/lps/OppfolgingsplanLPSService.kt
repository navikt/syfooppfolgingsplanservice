package no.nav.syfo.lps

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import generated.DataBatch
import no.nav.helse.op2016.Oppfoelgingsplan4UtfyllendeInfoM
import no.nav.helse.op2016.Skjemainnhold
import no.nav.syfo.domain.*
import no.nav.syfo.lps.database.*
import no.nav.syfo.lps.kafka.OppfolgingsplanLPSNAVProducer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oppfolgingsplan.avro.KOppfolgingsplanLPSNAV
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.pdl.isKode6Or7
import no.nav.syfo.service.*
import no.nav.syfo.util.InnsendingFeiletException
import no.nav.syfo.util.OppslagFeiletException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.web.client.HttpServerErrorException
import java.io.StringReader
import java.time.LocalDate
import java.util.*
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

@Repository
class OppfolgingsplanLPSService @Inject constructor(
    private val fastlegeService: FastlegeService,
    private val journalforOPService: JournalforOPService,
    private val oppfolgingsplanLPSNAVProducer: OppfolgingsplanLPSNAVProducer,
    private val metrikk: Metrikk,
    private val oppfolgingsplanLPSDAO: OppfolgingsplanLPSDAO,
    private val oppfolgingsplanLPSRetryService: OppfolgingsplanLPSRetryService,
    private val opPdfGenConsumer: OPPdfGenConsumer,
    private val pdlConsumer: PdlConsumer,
    private val feiletSendingService: FeiletSendingService
) {
    private val log = LoggerFactory.getLogger(OppfolgingsplanLPSService::class.java)

    fun getSharedWithNAV(
        fodselsnummer: Fodselsnummer
    ): List<OppfolgingsplanLPS> {
        return oppfolgingsplanLPSDAO.get(fodselsnummer)
            .filter { it.deltMedNav }
            .filter { it.pdf != null }
            .map {
                it.mapToOppfolgingsplanLPS()
            }
    }

    fun get(
        oppfolgingsplanLPSUUID: UUID
    ): OppfolgingsplanLPS {
        return oppfolgingsplanLPSDAO.get(oppfolgingsplanLPSUUID).mapToOppfolgingsplanLPS()
    }

    fun receivePlan(
        archiveReference: String,
        recordBatch: String,
        isRetry: Boolean
    ) {
        val dataBatch = dataBatchUnmarshaller.unmarshal(StringReader(recordBatch)) as DataBatch
        val payload = dataBatch.dataUnits.dataUnit.first().formTask.form.first().formData
        val oppfolgingsplan = xmlMapper.readValue<Oppfoelgingsplan4UtfyllendeInfoM>(payload)
        val skjemainnhold = oppfolgingsplan.skjemainnhold
        val virksomhetsnummer = Virksomhetsnummer(skjemainnhold.arbeidsgiver.orgnr)

        processPlan(
            archiveReference,
            recordBatch,
            skjemainnhold,
            virksomhetsnummer,
            isRetry
        )
        metrikk.tellHendelse("prosessering_av_lps_plan_vellykket")
    }

    private fun processPlan(
        archiveReference: String,
        batch: String,
        skjemainnhold: Skjemainnhold,
        virksomhetsnummer: Virksomhetsnummer,
        isRetry: Boolean
    ) {
        val incomingMetadata = IncomingMetadata(
            archiveReference = archiveReference,
            senderOrgName = skjemainnhold.arbeidsgiver.orgnavn,
            senderOrgId = skjemainnhold.arbeidsgiver.orgnr,
            userPersonNumber = skjemainnhold.sykmeldtArbeidstaker.fnr
        )
        try {
            val isUserDiskresjonsmerket = pdlConsumer.person(incomingMetadata.userPersonNumber)?.isKode6Or7()
            if (isUserDiskresjonsmerket == null) {
                storePlanForRetry(incomingMetadata, batch)
            } else if (isUserDiskresjonsmerket) {
                log.warn("Received Oppfolgingsplan from LPS for a person that is denied access to Oppfolgingsplan")
                metrikk.tellHendelse("lps_plan_diskresjonsmerket")
                if (isRetry) {
                    oppfolgingsplanLPSRetryService.delete(incomingMetadata.archiveReference)
                }
                return
            } else {
                val idList: Pair<Long, UUID> = savePlan(
                    batch,
                    skjemainnhold,
                    virksomhetsnummer
                )
                if (isRetry) {
                    oppfolgingsplanLPSRetryService.delete(incomingMetadata.archiveReference)
                }

                val lpsPdfModel = mapFormdataToFagmelding(skjemainnhold, incomingMetadata)
                val pdf = opPdfGenConsumer.pdfgenResponse(lpsPdfModel)
                oppfolgingsplanLPSDAO.updatePdf(idList.first, pdf)
                log.info("KAFKA-trace: pdf generated and stored")

                if (skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTiNav == true) {
                    val kOppfolgingsplanLPSNAV = KOppfolgingsplanLPSNAV(
                        idList.second.toString(),
                        incomingMetadata.userPersonNumber,
                        virksomhetsnummer.value,
                        lpsPdfModel.oppfolgingsplan.isBehovForBistandFraNAV(),
                        LocalDate.now().toEpochDay().toInt()
                    )
                    metrikk.tellHendelseMedTag("lps_plan_behov_for_bistand_fra_nav", "bistand", lpsPdfModel.oppfolgingsplan.isBehovForBistandFraNAV())
                    oppfolgingsplanLPSNAVProducer.sendOppfolgingsLPSTilNAV(kOppfolgingsplanLPSNAV)
                }
                if (skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTilFastlege == true) {
                    sendLpsOppfolgingsplanTilFastlege(incomingMetadata.userPersonNumber, pdf, idList.first, 0)
                }
            }
        } catch (e: HttpServerErrorException) {
            log.error("Could not process LPS-plan due to server error: ${e.message}")
            storePlanForRetry(incomingMetadata, batch)
        }
    }

    private fun storePlanForRetry(incomingMetadata: IncomingMetadata, batch: String) {
        oppfolgingsplanLPSRetryService.getOrCreate(incomingMetadata.archiveReference, batch)
        log.warn("Diskresjonskode was not received from PDL and LPS-plan is stored for retry.")
        metrikk.tellHendelse("lps_plan_retry_created")
    }

    private fun savePlan(
        batch: String,
        skjemainnhold: Skjemainnhold,
        virksomhetsnummer: Virksomhetsnummer
    ): Pair<Long, UUID> {
        return oppfolgingsplanLPSDAO.create(
            arbeidstakerFnr = Fodselsnummer(skjemainnhold.sykmeldtArbeidstaker.fnr),
            virksomhetsnummer = virksomhetsnummer.value,
            xml = batch,
            delt_med_nav = skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTiNav ?: false,
            del_med_fastlege = skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTilFastlege ?: false,
            delt_med_fastlege = false
        )
    }

    fun retrySendLpsPlanTilFastlege(
        feiletSending: FeiletSending
    ) {
        val oppfolgingsplan: POppfolgingsplanLPS = oppfolgingsplanLPSDAO.get(feiletSending.oppfolgingsplanId)

        if (oppfolgingsplan.pdf != null) {
            log.info("Prøver å sende oppfolgingsplan med id {} på nytt.", oppfolgingsplan.id)
            sendLpsOppfolgingsplanTilFastlege(oppfolgingsplan.fnr, oppfolgingsplan.pdf, oppfolgingsplan.id, feiletSending.number_of_tries)
        }
    }

    private fun sendLpsOppfolgingsplanTilFastlege(
        fnr: String,
        pdf: ByteArray,
        oppfolgingsplanId: Long,
        try_num: Int
    ) {
        try {
            fastlegeService.sendOppfolgingsplanLPS(fnr, pdf)
            oppfolgingsplanLPSDAO.updateSharedWithFastlege(oppfolgingsplanId)
            if (try_num > 0) {
                metrikk.tellHendelse("lps_plan_delt_etter_feilet_sending")
                feiletSendingService.fjernSendtOppfolgingsplan(oppfolgingsplanId)
            }
        } catch (e: InnsendingFeiletException) {
            log.error("Fanget InnsendingFeiletException", e)
            feiletSendingService.opprettEllerOppdaterFeiletSending(oppfolgingsplanId, try_num)
        } catch (e: OppslagFeiletException) {
            log.warn("Fanget OppslagFeiletException", e)
            feiletSendingService.opprettEllerOppdaterFeiletSending(oppfolgingsplanId, try_num)
        } catch (e: Exception) {
            log.error("Fanget uventet exception", e)
            feiletSendingService.opprettEllerOppdaterFeiletSending(oppfolgingsplanId, try_num)
        }
    }

    fun createOppfolgingsplanLPSJournalposter() {
        oppfolgingsplanLPSDAO.getPlanListToJournalpost().map {
            it.mapToOppfolgingsplanLPS()
        }.forEach { planLPS ->
            val journalpostId = journalforOPService.createJournalpostPlanLPS(planLPS).toString()
            oppfolgingsplanLPSDAO.updateJournalpostId(
                planLPS.id,
                journalpostId
            )
            metrikk.tellHendelse("plan_lps_opprettet_journal_gosys")
        }
    }
}
