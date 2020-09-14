package no.nav.syfo.lps.batch

import no.nav.syfo.domain.FeiletSending
import no.nav.syfo.lps.OppfolgingsplanLPSService
import no.nav.syfo.repository.dao.FeiletSendingDAO
import no.nav.syfo.util.PropertyUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class OppfolgingsplanLPS @Inject constructor(
    @Value("\${nais.cluster.name}") private val naisClusterName: String,
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService,
    private val feiletSendingDAO: FeiletSendingDAO
) {
    private val isDev = naisClusterName == "dev-fss"

    @Scheduled(fixedDelay = 60000)
    fun retrySendOppfolgingsplanLpsTilFastlege() {
        if (isDev && "true" != System.getProperty(PropertyUtil.LOCAL_MOCK)) {
            val liste: List<FeiletSending> = feiletSendingDAO.hentFeiledeSendinger()

            for(feiletSending in liste) {
                oppfolgingsplanLPSService.retrySendLpsPlanTilFastlege(feiletSending)
            }
        }
    }
}
