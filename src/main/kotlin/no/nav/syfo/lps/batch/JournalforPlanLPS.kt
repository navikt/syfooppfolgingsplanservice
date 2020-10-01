package no.nav.syfo.lps.batch

import no.nav.syfo.lps.OppfolgingsplanLPSService
import no.nav.syfo.service.LeaderElectionService
import no.nav.syfo.util.PropertyUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class JournalforPlanLPS @Inject constructor(
    @Value("\${nais.cluster.name}") private val naisClusterName: String,
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService,
    private val leaderElectionService: LeaderElectionService
) {
    private val isDev = naisClusterName == "dev-fss"

    @Scheduled(fixedRate = 60 * 60 * 1000)
    fun createOppfolgingsplanLPSJournalposter() {
        if (leaderElectionService.isLeader && "true" != System.getProperty(PropertyUtil.LOCAL_MOCK)) {
            oppfolgingsplanLPSService.createOppfolgingsplanLPSJournalposter()
        }
    }
}
