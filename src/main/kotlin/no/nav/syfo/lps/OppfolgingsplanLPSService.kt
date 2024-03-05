package no.nav.syfo.lps

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.helse.op2016.Oppfoelgingsplan4UtfyllendeInfoM
import no.nav.helse.op2016.Skjemainnhold
import no.nav.syfo.dialogmelding.DialogmeldingService
import no.nav.syfo.domain.FeiletSending
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.lps.database.OppfolgingsplanLPSDAO
import no.nav.syfo.lps.database.POppfolgingsplanLPS
import no.nav.syfo.lps.database.mapToOppfolgingsplanLPS
import no.nav.syfo.lps.kafka.KOppfolgingsplanLPS
import no.nav.syfo.lps.kafka.OppfolgingsplanLPSProducer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.service.FeiletSendingService
import no.nav.syfo.service.JournalforOPService
import no.nav.syfo.util.InnsendingFeiletException
import no.nav.syfo.util.OppslagFeiletException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

val xmlMapper: ObjectMapper = XmlMapper(
    JacksonXmlModule().apply {
        setDefaultUseWrapper(false)
    },
).registerModule(JaxbAnnotationModule())
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
    private val feiletSendingService: FeiletSendingService,
) {
    private val log = LoggerFactory.getLogger(OppfolgingsplanLPSService::class.java)

    fun getSharedWithNAV(
        fodselsnummer: Fodselsnummer,
    ): List<OppfolgingsplanLPS> {
        return oppfolgingsplanLPSDAO.get(fodselsnummer)
            .filter { it.deltMedNav }
            .filter { it.pdf != null }
            .map {
                it.mapToOppfolgingsplanLPS()
            }
    }

    fun get(
        oppfolgingsplanLPSUUID: UUID,
    ): OppfolgingsplanLPS {
        return oppfolgingsplanLPSDAO.get(oppfolgingsplanLPSUUID).mapToOppfolgingsplanLPS()
    }

    fun retryGeneratePDF(id: Long, xml: String, archiveReference: String) {
        log.info("Try to generate PDF for plan with Id: $id")

        val oppfolgingsplan = xmlMapper.readValue<Oppfoelgingsplan4UtfyllendeInfoM>(xml)
        val skjemainnhold = oppfolgingsplan.skjemainnhold

        val incomingMetadata = IncomingMetadata(
            archiveReference = archiveReference,
            senderOrgName = skjemainnhold.arbeidsgiver.orgnavn,
            senderOrgId = skjemainnhold.arbeidsgiver.orgnr,
            userPersonNumber = skjemainnhold.sykmeldtArbeidstaker.fnr,
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
        payload: String,
        isRetry: Boolean,
    ) {
        val oppfolgingsplan = xmlMapper.readValue<Oppfoelgingsplan4UtfyllendeInfoM>(payload)
        val skjemainnhold = oppfolgingsplan.skjemainnhold
        val virksomhetsnummer = Virksomhetsnummer(skjemainnhold.arbeidsgiver.orgnr)

        processPlan(
            archiveReference,
            payload,
            skjemainnhold,
            virksomhetsnummer,
            isRetry,
        )
        metrikk.tellHendelse(METRIKK_PROSSESERING_VELLYKKET)
    }

    private fun processPlan(
        archiveReference: String,
        payload: String,
        skjemainnhold: Skjemainnhold,
        virksomhetsnummer: Virksomhetsnummer,
        isRetry: Boolean,
    ) {
        val shouldDistributePlan = System.getenv("DISTRIBUTE_ALTINN_PLANS") == "true"

        val incomingMetadata = IncomingMetadata(
            archiveReference = archiveReference,
            senderOrgName = skjemainnhold.arbeidsgiver.orgnavn,
            senderOrgId = skjemainnhold.arbeidsgiver.orgnr,
            userPersonNumber = skjemainnhold.sykmeldtArbeidstaker.fnr,
        )

        val skjemaFnr = incomingMetadata.userPersonNumber
        val (gjeldendeFnr, pdlCallFailed) = gjeldendeFnr(skjemaFnr)
        if (pdlCallFailed) {
            val errorMessage = "Unable to determine current fnr: PDL call 'hentIdenter' failed"
            storePlanForRetry(incomingMetadata, payload, errorMessage)
            return
        }

        val idList: Pair<Long, UUID> = savePlan(
            gjeldendeFnr,
            payload,
            skjemainnhold,
            virksomhetsnummer,
            archiveReference,
        )

        if (isRetry) {
            oppfolgingsplanLPSRetryService.delete(incomingMetadata.archiveReference)
        }

        val lpsPdfModel = mapFormdataToFagmelding(gjeldendeFnr, skjemainnhold, incomingMetadata)
        val pdf = opPdfGenConsumer.pdfgenResponse(lpsPdfModel)
        oppfolgingsplanLPSDAO.updatePdf(idList.first, pdf)
        log.info("KAFKA-trace: pdf generated and stored")

        if (shouldDistributePlan && skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTiNav == true) {
            val kOppfolgingsplanLPS = KOppfolgingsplanLPS(
                idList.second.toString(),
                gjeldendeFnr,
                virksomhetsnummer.value,
                lpsPdfModel.oppfolgingsplan.isBehovForBistandFraNAV(),
                LocalDate.now().toEpochDay().toInt(),
            )
            metrikk.tellHendelseMedTag(
                METRIKK_BISTAND_FRA_NAV,
                METRIKK_TAG_BISTAND,
                lpsPdfModel.oppfolgingsplan.isBehovForBistandFraNAV(),
            )
            oppfolgingsplanLPSProducer.sendOppfolgingsLPSTilNAV(kOppfolgingsplanLPS)
        }
        if (shouldDistributePlan && skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTilFastlege == true) {
            sendLpsOppfolgingsplanTilFastlege(incomingMetadata.userPersonNumber, pdf, idList.first, 0)
        }
    }

    private fun storePlanForRetry(incomingMetadata: IncomingMetadata, payload: String, errorMessage: String) {
        oppfolgingsplanLPSRetryService.getOrCreate(incomingMetadata.archiveReference, payload)
        log.warn(errorMessage)
        metrikk.tellHendelse(METRIKK_LPS_RETRY)
    }

    private fun savePlan(
        fnr: String,
        payload: String,
        skjemainnhold: Skjemainnhold,
        virksomhetsnummer: Virksomhetsnummer,
        archiveReference: String,
    ): Pair<Long, UUID> {
        return oppfolgingsplanLPSDAO.create(
            arbeidstakerFnr = Fodselsnummer(fnr),
            virksomhetsnummer = virksomhetsnummer.value,
            xml = payload,
            deltMedNAV = skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTiNav ?: false,
            delMedFastlege = skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTilFastlege ?: false,
            deltMedFastlege = false,
            archiveReference = archiveReference,
        )
    }

    private fun gjeldendeFnr(fnr: String): Pair<String, Boolean> {
        return try {
            val gjeldendeFnr = pdlConsumer.gjeldendeFnr(fnr)
            if (gjeldendeFnr != fnr) {
                metrikk.tellHendelse(METRIKK_OLD_FNR)
            }
            Pair(gjeldendeFnr, false)
        } catch (e: RuntimeException) {
            Pair("", true)
        }
    }

    fun retrySendLpsPlanTilFastlege(
        feiletSending: FeiletSending,
    ) {
        val oppfolgingsplan: POppfolgingsplanLPS = oppfolgingsplanLPSDAO.get(feiletSending.oppfolgingsplanId)

        if (oppfolgingsplan.pdf != null) {
            log.info("Prøver å sende oppfolgingsplan med id {} på nytt.", oppfolgingsplan.id)
            sendLpsOppfolgingsplanTilFastlege(
                oppfolgingsplan.fnr,
                oppfolgingsplan.pdf,
                oppfolgingsplan.id,
                feiletSending.number_of_tries,
            )
        }
    }

    private fun sendLpsOppfolgingsplanTilFastlege(
        fnr: String,
        pdf: ByteArray,
        oppfolgingsplanId: Long,
        try_num: Int,
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
                journalpostId,
            )
            metrikk.tellHendelse(METRIKK_LPS_JOURNALFORT_TIL_GOSYS)
        }
    }

    companion object {
        val METRIKK_PROSSESERING_VELLYKKET = "prosessering_av_lps_plan_vellykket"
        val METRIKK_LPS_RETRY = "lps_plan_retry_created"
        val METRIKK_OLD_FNR = "lps_plan_old_fnr"
        val METRIKK_DELT_MED_FASTLEGE_ETTER_FEILET_SENDING = "lps_plan_delt_etter_feilet_sending"
        val METRIKK_DELT_MED_FASTLEGE = "lps_plan_delt"
        val METRIKK_LPS_JOURNALFORT_TIL_GOSYS = "plan_lps_opprettet_journal_gosys"
        val METRIKK_BISTAND_FRA_NAV = "lps_plan_behov_for_bistand_fra_nav"
        val METRIKK_TAG_BISTAND = "bistand"
    }
}
