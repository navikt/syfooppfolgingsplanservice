package no.nav.syfo.lps.batch

import no.nav.syfo.lps.OppfolgingsplanLPSService
import no.nav.syfo.service.LeaderElectionService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class JournalforPlanLPS @Inject constructor(
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService,
    private val leaderElectionService: LeaderElectionService
) {

    @Scheduled(fixedRate = 60 * 10 * 1000)
    fun createOppfolgingsplanLPSJournalposter() {
        if (leaderElectionService.isLeader) {
            oppfolgingsplanLPSService.createOppfolgingsplanLPSJournalposter()
        }
    }
}
