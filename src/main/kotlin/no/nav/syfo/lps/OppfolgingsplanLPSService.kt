package no.nav.syfo.lps

import no.nav.helse.op2016.Skjemainnhold
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.domain.Virksomhetsnummer
import no.nav.syfo.lps.database.OppfolgingsplanLPSDAO
import no.nav.syfo.lps.database.mapToOppfolgingsplanLPS
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.FastlegeService
import no.nav.syfo.service.JournalforOPService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.*
import javax.inject.Inject

@Repository
class OppfolgingsplanLPSService @Inject constructor(
    private val fastlegeService: FastlegeService,
    private val journalforOPService: JournalforOPService,
    private val metrikk: Metrikk,
    private val oppfolgingsplanLPSDAO: OppfolgingsplanLPSDAO,
    private val opPdfGenConsumer: OPPdfGenConsumer
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
        val planId = savePlan(
            batch,
            skjemainnhold,
            virksomhetsnummer
        )

        val incomingMetadata = IncomingMetadata(
            archiveReference = archiveReference,
            senderOrgName = skjemainnhold.arbeidsgiver.orgnavn,
            senderOrgId = skjemainnhold.arbeidsgiver.orgnr,
            userPersonNumber = skjemainnhold.sykmeldtArbeidstaker.fnr
        )

        val lpsPdfModel = mapFormdataToFagmelding(skjemainnhold, incomingMetadata)
        val pdf = opPdfGenConsumer.pdfgenResponse(lpsPdfModel)
        oppfolgingsplanLPSDAO.updatePdf(planId, pdf)
        log.info("KAFKA-trace: pdf generated and stored")
        if (skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTilFastlege) {
            fastlegeService.sendOppfolgingsplanLPS(incomingMetadata.userPersonNumber, pdf)
            oppfolgingsplanLPSDAO.updateSharedFastlege(planId)
        }
    }

    private fun savePlan(
        batch: String,
        skjemainnhold: Skjemainnhold,
        virksomhetsnummer: Virksomhetsnummer
    ): Long {
        return oppfolgingsplanLPSDAO.create(
            arbeidstakerFnr = Fodselsnummer(skjemainnhold.sykmeldtArbeidstaker.fnr),
            virksomhetsnummer = virksomhetsnummer.value,
            xml = batch,
            delt_med_nav = skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTiNav,
            del_med_fastlege = skjemainnhold.mottaksInformasjon.isOppfolgingsplanSendesTilFastlege,
            delt_med_fastlege = false
        )
    }

    fun createOppfolgingsplanLPSJournalposter() {
        oppfolgingsplanLPSDAO.getPlanListToJournalpost().map {
            it.mapToOppfolgingsplanLPS()
        }.forEach { planLPS ->
            val journalpostId = journalforOPService.createJournalpostPlanLPS(planLPS).toString();
            oppfolgingsplanLPSDAO.updateJournalpostId(
                planLPS.id,
                journalpostId
            )
            metrikk.tellHendelse("plan_lps_opprettet_journal_gosys")
        }
    }
}
