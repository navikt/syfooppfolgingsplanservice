package no.nav.syfo.lps.batch

import no.nav.syfo.domain.FeiletSending
import no.nav.syfo.lps.OppfolgingsplanLPSService
import no.nav.syfo.repository.dao.FeiletSendingDAO
import no.nav.syfo.service.LeaderElectionService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.inject.Inject

const val TWENTY_MINUTES_MILLISECONDS : Long = 20 * 60 * 1000

@Service
class OppfolgingsplanLPS @Inject constructor(
    private val leaderElectionService: LeaderElectionService,
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService,
    private val feiletSendingDAO: FeiletSendingDAO
) {

    @Scheduled(fixedDelay = TWENTY_MINUTES_MILLISECONDS)
    fun retrySendOppfolgingsplanLpsTilFastlege() {
        if (leaderElectionService.isLeader) {
            val liste: List<FeiletSending> = feiletSendingDAO.hentFeiledeSendinger()

            for(feiletSending in liste) {
                oppfolgingsplanLPSService.retrySendLpsPlanTilFastlege(feiletSending)
            }
        }
    }
}
