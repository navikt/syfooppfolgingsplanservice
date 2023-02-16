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
import no.nav.syfo.dialogmelding.DialogmeldingService
import no.nav.syfo.domain.*
import no.nav.syfo.lps.database.*
import no.nav.syfo.lps.kafka.KOppfolgingsplanLPS
import no.nav.syfo.lps.kafka.OppfolgingsplanLPSProducer
import no.nav.syfo.metric.Metrikk
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
    private val dialogmeldingService: DialogmeldingService,
    private val journalforOPService: JournalforOPService,
    private val oppfolgingsplanLPSProducer: OppfolgingsplanLPSProducer,
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

    fun retryGeneratePDF(id: Long, recordBatch: String) {
        log.info("Try to generate PDF for plan with Id: $id")

        val dataBatch = dataBatchUnmarshaller.unmarshal(StringReader(recordBatch)) as DataBatch
        val dataUnit = dataBatch.dataUnits.dataUnit.first()
        val payload = dataUnit.formTask.form.first().formData
        val oppfolgingsplan = xmlMapper.readValue<Oppfoelgingsplan4UtfyllendeInfoM>(payload)
        val skjemainnhold = oppfolgingsplan.skjemainnhold

        val archiveReference = dataUnit.archiveReference

        val incomingMetadata = IncomingMetadata(
            archiveReference = archiveReference,
            senderOrgName = skjemainnhold.arbeidsgiver.orgnavn,
            senderOrgId = skjemainnhold.arbeidsgiver.orgnr,
            userPersonNumber = skjemainnhold.sykmeldtArbeidstaker.fnr
        )

        val skjemaFnr = incomingMetadata.userPersonNumber
        val (gjeldendeFnr, pdlCallFailed) = gjeldendeFnr(skjemaFnr)
        if (pdlCallFailed) {
            log.error("Unable to generate PDF for plan with id: $id, calling PDL failed..")
            return
        }
        val lpsPdfModel = mapFormdataToFagmelding(gjeldendeFnr, skjemainnhold, incomingMetadata)
        val pdf = opPdfGenConsumer.pdfgenResponse(lpsPdfModel)
        oppfolgingsplanLPSDAO.updatePdf(id, pdf)
        log.info("PDF generation retry successful for plan with id: $id")
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
        metrikk.tellHendelse(METRIKK_PROSSESERING_VELLYKKET)
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

        val isUserDiskresjonsmerket = try {
            pdlConsumer.person(incomingMetadata.userPersonNumber)?.isKode6Or7()
        } catch (e: HttpServerErrorException) {
            log.error("Could not process LPS-plan due to server error: ${e.message}")
            null
        } catch (e: RuntimeException) {
            log.error("Could not process LPS-plan due to error: ${e.message}")
            null
        }

        if (isUserDiskresjonsmerket == null) {
            val errorMessage = "Diskresjonskode was not received from PDL and LPS-plan is stored for retry."
            storePlanForRetry(incomingMetadata, batch, errorMessage)
        } else if (isUserDiskresjonsmerket) {
            log.warn("Received Oppfolgingsplan from LPS for a person that is denied access to Oppfolgingsplan")
            metrikk.tellHendelse(METRIKK_DISKRESJONSMERKET)
            if (isRetry) {
                oppfolgingsplanLPSRetryService.delete(incomingMetadata.archiveReference)
            }
            return
        } else {
            val skjemaFnr = incomingMetadata.userPersonNumber
            val (gjeldendeFnr, pdlCallFailed) = gjeldendeFnr(skjemaFnr)
            if (pdlCallFailed) {
                val errorMessage = "Unable to determine current fnr: PDL call 'hentIdenter' failed"
                storePlanForRetry(incomingMetadata, batch, errorMessage)
                return
            }

            val idList: Pair<Long, UUID> = savePlan(
                gjeldendeFnr,
                batch,
                skjemainnhold,
                virksomhetsnummer
            )

            if (isRetry) {
                oppfolgingsplanLPSRetryService.delete(incomingMetadata.archiveReference)
            }

            val lpsPdfModel = mapFormdataToFagmelding(gjeldendeFnr, skjemainnhold, incomingMetadata)
            val pdf = opPdfGenConsumer.pdfgenResponse(lpsPdfModel)
            oppfolgingsplanLPSDAO.updatePdf(idList.first, pdf)
            log.info("KAFKA-trace: pdf generated and stored")

            if (skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTiNav == true) {
                val kOppfolgingsplanLPS = KOppfolgingsplanLPS(
                    idList.second.toString(),
                    gjeldendeFnr,
                    virksomhetsnummer.value,
                    lpsPdfModel.oppfolgingsplan.isBehovForBistandFraNAV(),
                    LocalDate.now().toEpochDay().toInt()
                )
                metrikk.tellHendelseMedTag(METRIKK_BISTAND_FRA_NAV, METRIKK_TAG_BISTAND, lpsPdfModel.oppfolgingsplan.isBehovForBistandFraNAV())
                oppfolgingsplanLPSProducer.sendOppfolgingsLPSTilNAV(kOppfolgingsplanLPS)
            }
            if (skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTilFastlege == true) {
                sendLpsOppfolgingsplanTilFastlege(incomingMetadata.userPersonNumber, pdf, idList.first, 0)
            }
        }
    }

    private fun storePlanForRetry(incomingMetadata: IncomingMetadata, batch: String, errorMessage: String) {
        oppfolgingsplanLPSRetryService.getOrCreate(incomingMetadata.archiveReference, batch)
        log.warn(errorMessage)
        metrikk.tellHendelse(METRIKK_LPS_RETRY)
    }

    private fun savePlan(
        fnr: String,
        batch: String,
        skjemainnhold: Skjemainnhold,
        virksomhetsnummer: Virksomhetsnummer
    ): Pair<Long, UUID> {
        return oppfolgingsplanLPSDAO.create(
            arbeidstakerFnr = Fodselsnummer(fnr),
            virksomhetsnummer = virksomhetsnummer.value,
            xml = batch,
            delt_med_nav = skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTiNav ?: false,
            del_med_fastlege = skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTilFastlege ?: false,
            delt_med_fastlege = false
        )
    }

    private fun gjeldendeFnr(fnr: String): Pair<String, Boolean> {
        return try {
            val gjeldendeFnr = pdlConsumer.gjeldendeFnr(fnr)
            if (gjeldendeFnr != fnr)
                metrikk.tellHendelse(METRIKK_OLD_FNR)
            Pair(gjeldendeFnr, false)
        } catch (e: RuntimeException) {
            Pair("", true)
        }
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
            dialogmeldingService.sendOppfolgingsplanLPSTilFastlege(fnr, pdf)
            oppfolgingsplanLPSDAO.updateSharedWithFastlege(oppfolgingsplanId)
            if (try_num > 0) {
                metrikk.tellHendelse(METRIKK_DELT_MED_FASTLEGE_ETTER_FEILET_SENDING)
                feiletSendingService.fjernSendtOppfolgingsplan(oppfolgingsplanId)
            } else {
                metrikk.tellHendelse(METRIKK_DELT_MED_FASTLEGE)
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
            metrikk.tellHendelse(METRIKK_LPS_JOURNALFORT_TIL_GOSYS)
        }
    }

    companion object {
        val METRIKK_PROSSESERING_VELLYKKET = "prosessering_av_lps_plan_vellykket"
        val METRIKK_DISKRESJONSMERKET = "lps_plan_diskresjonsmerket"
        val METRIKK_LPS_RETRY = "lps_plan_retry_created"
        val METRIKK_OLD_FNR = "lps_plan_old_fnr"
        val METRIKK_DELT_MED_FASTLEGE_ETTER_FEILET_SENDING = "lps_plan_delt_etter_feilet_sending"
        val METRIKK_DELT_MED_FASTLEGE = "lps_plan_delt"
        val METRIKK_LPS_JOURNALFORT_TIL_GOSYS = "plan_lps_opprettet_journal_gosys"
        val METRIKK_BISTAND_FRA_NAV = "lps_plan_behov_for_bistand_fra_nav"
        val METRIKK_TAG_BISTAND = "bistand"
    }
}
