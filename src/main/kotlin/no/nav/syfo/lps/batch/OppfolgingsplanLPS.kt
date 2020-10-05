package no.nav.syfo.lps.batch

import no.nav.syfo.domain.FeiletSending
import no.nav.syfo.lps.OppfolgingsplanLPSService
import no.nav.syfo.repository.dao.FeiletSendingDAO
import no.nav.syfo.service.LeaderElectionService
import no.nav.syfo.util.PropertyUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class OppfolgingsplanLPS @Inject constructor(
    private val leaderElectionService: LeaderElectionService,
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService,
    private val feiletSendingDAO: FeiletSendingDAO
) {

    @Scheduled(fixedDelay = 60 * 1000)
    fun retrySendOppfolgingsplanLpsTilFastlege() {
        if (leaderElectionService.isLeader && "true" != System.getProperty(PropertyUtil.LOCAL_MOCK)) {
            val liste: List<FeiletSending> = feiletSendingDAO.hentFeiledeSendinger()

            for(feiletSending in liste) {
                oppfolgingsplanLPSService.retrySendLpsPlanTilFastlege(feiletSending)
            }
        }
    }
}
