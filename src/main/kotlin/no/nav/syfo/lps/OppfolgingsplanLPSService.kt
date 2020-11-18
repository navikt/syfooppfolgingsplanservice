package no.nav.syfo.lps

import no.nav.helse.op2016.Skjemainnhold
import no.nav.syfo.domain.FeiletSending
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.lps.database.*
import no.nav.syfo.lps.kafka.OppfolgingsplanLPSNAVProducer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oppfolgingsplan.avro.KOppfolgingsplanLPSNAV
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.pdl.isKode6Or7
import no.nav.syfo.service.FastlegeService
import no.nav.syfo.service.FeiletSendingService
import no.nav.syfo.service.JournalforOPService
import no.nav.syfo.util.InnsendingFeiletException
import no.nav.syfo.util.OppslagFeiletException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@Repository
class OppfolgingsplanLPSService @Inject constructor(
    private val fastlegeService: FastlegeService,
    private val journalforOPService: JournalforOPService,
    private val oppfolgingsplanLPSNAVProducer: OppfolgingsplanLPSNAVProducer,
    private val metrikk: Metrikk,
    private val oppfolgingsplanLPSDAO: OppfolgingsplanLPSDAO,
    private val oppfolgingsplanLPSRetryDAO: OppfolgingsplanLPSRetryDAO,
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
        batch: String,
        skjemainnhold: Skjemainnhold,
        virksomhetsnummer: Virksomhetsnummer
    ) {
        val incomingMetadata = IncomingMetadata(
            archiveReference = archiveReference,
            senderOrgName = skjemainnhold.arbeidsgiver.orgnavn,
            senderOrgId = skjemainnhold.arbeidsgiver.orgnr,
            userPersonNumber = skjemainnhold.sykmeldtArbeidstaker.fnr
        )

        val isUserDiskresjonsmerket = pdlConsumer.person(incomingMetadata.userPersonNumber)?.isKode6Or7()
        if (isUserDiskresjonsmerket == null) {
            log.warn("Diskresjosnkode was not received from PDL and LPS-plan is stored in retry table.")
            saveLPSPlanRetry(incomingMetadata.archiveReference, batch)
            metrikk.tellHendelse("lps_plan_retry_created")
        } else if (isUserDiskresjonsmerket) {
            log.warn("Received Oppfolgingsplan from LPS for a person that is denied access to Oppfolgingsplan")
            metrikk.tellHendelse("lps_plan_diskresjonsmerket")
            return
        } else {
            val idList: Pair<Long, UUID> = savePlan(
                batch,
                skjemainnhold,
                virksomhetsnummer
            )

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
                metrikk.tellHendelseMedTag("lps_plan_behov_for_bistand_fra_nav", "bistand",  lpsPdfModel.oppfolgingsplan.isBehovForBistandFraNAV())
                oppfolgingsplanLPSNAVProducer.sendOppfolgingsLPSTilNAV(kOppfolgingsplanLPSNAV)
            }
            if (skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTilFastlege == true) {
                sendLpsOppfolgingsplanTilFastlege(incomingMetadata.userPersonNumber, pdf, idList.first, 0)
            }
        }
    }

    private fun saveLPSPlanRetry(
        archiveReference: String,
        xml: String
    ): Long {
        return oppfolgingsplanLPSRetryDAO.create(
            archiveReference = archiveReference,
            xml = xml
        )
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
            if(try_num > 0) {
                metrikk.tellHendelse("lps_plan_delt_etter_feilet_sending")
                feiletSendingService.fjernSendtOppfolgingsplan(oppfolgingsplanId)
            }
        } catch (e: InnsendingFeiletException) {
            log.error("Fanget InnsendingFeiletException", e)
            feiletSendingService.opprettEllerOppdaterFeiletSending(oppfolgingsplanId, try_num)
        } catch (e: OppslagFeiletException) {
            log.error("Fanget OppslagFeiletException", e)
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
