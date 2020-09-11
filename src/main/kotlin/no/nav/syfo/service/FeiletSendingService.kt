package no.nav.syfo.service

import no.nav.syfo.domain.FeiletSending
import no.nav.syfo.repository.dao.FeiletSendingDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FeiletSendingService @Autowired constructor(private val feiletSendingDAO: FeiletSendingDAO) {
    fun opprettEllerOppdatertFeiletSending(oppfolgingsplanId: Long, number_of_tries: Int) {
        if (number_of_tries == 0) {
            feiletSendingDAO.create(oppfolgingsplanId = oppfolgingsplanId, max_retries = FeiletSending.MAX_RETRIES)
            log.info("Fikk ikke sendt oppfolgingsplan med id {} til fastlege. Lagrer og prøver igjen senere.", oppfolgingsplanId)
        } else {
            val feiletSending = feiletSendingDAO.findByOppfolgingsplanId(oppfolgingsplanId)
            feiletSendingDAO.updateAfterRetry(feiletSending.copy(number_of_tries = number_of_tries + 1))
            log.info("Fikk ikke sendt oppfolgingsplan med id {} til fastlege etter {} forsøk. Prøver igjen senere", oppfolgingsplanId, number_of_tries)
        }
    }

    fun fjernSendtOppfolgingsplan(oppfolgingsplanId: Long) {
        feiletSendingDAO.remove(oppfolgingsplanId)
        log.info("Fikk sendt oppfolgingsplan med id {} etter nytt forsøk.", oppfolgingsplanId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(FeiletSendingService::class.java)
    }
}