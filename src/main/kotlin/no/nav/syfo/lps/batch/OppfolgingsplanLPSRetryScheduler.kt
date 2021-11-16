package no.nav.syfo.lps.batch

import no.nav.syfo.lps.OppfolgingsplanLPSService
import no.nav.syfo.lps.database.OppfolgingsplanLPSDAO
import no.nav.syfo.lps.database.OppfolgingsplanLPSRetryDAO
import no.nav.syfo.lps.database.POppfolgingsplanLPSRetry
import no.nav.syfo.service.LeaderElectionService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class OppfolgingsplanLPSRetryScheduler @Inject constructor(
    private val leaderElectionService: LeaderElectionService,
    private val oppfolgingsplanLPSRetryDAO: OppfolgingsplanLPSRetryDAO,
    private val oppfolgingsplanLPSDAO: OppfolgingsplanLPSDAO,
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService
) {

    @Scheduled(fixedDelay = FIFTEEN_MINUTES_MILLISECONDS)
    fun retryProcessOppfolgingsplanLPS() {
        if (leaderElectionService.isLeader) {

            if (!rekjorPlanFerdig) {
                LOG.info("REKJØR PLAN")
                val recordBatch = oppfolgingsplanLPSDAO.getBatch(id)
                oppfolgingsplanLPSService.receivePlan(ARref, recordBatch,false)
                rekjorPlanFerdig = true
            }


            val oppfolgingsplanLPSRetryList: List<POppfolgingsplanLPSRetry> = oppfolgingsplanLPSRetryDAO.get().shuffled()

            oppfolgingsplanLPSRetryList.forEach {
                LOG.info("Retrying OppfolgingsplanLPS with archiveReference=${it.archiveReference}")
                oppfolgingsplanLPSService.receivePlan(
                    archiveReference = it.archiveReference,
                    recordBatch = it.xml,
                    isRetry = true
                )
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OppfolgingsplanLPSRetryScheduler::class.java)
        var rekjorPlanFerdig = false
        val ARref = "AR432091821"
        val id = "23589"
        private const val FIFTEEN_MINUTES_MILLISECONDS: Long = 15 * 60 * 1000
    }
}

