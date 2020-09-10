package no.nav.syfo.service;

import no.nav.syfo.domain.FeiletSending;
import no.nav.syfo.repository.dao.FeiletSendingDAO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class FeiletSendingService {

    private static final Logger log = getLogger(FeiletSendingService.class);

    private final FeiletSendingDAO feiletSendingDAO;
    private static final int MAX_RETRIES = 3;

    @Autowired
    public FeiletSendingService(FeiletSendingDAO feiletSendingDAO) {
        this.feiletSendingDAO = feiletSendingDAO;
    }

    public void opprettEllerOppdatertFeiletSending(Long oppfolgingsplanId, int number_of_tries) {
        FeiletSending feiletSending = new FeiletSending()
                .oppfolgingsplanId(oppfolgingsplanId)
                .max_retries(MAX_RETRIES)
                .number_of_tries(number_of_tries+1);

        if (number_of_tries == 0) {
            feiletSendingDAO.create(feiletSending);
            log.info("Fikk ikke sendt oppfolgingsplan med id {} til fastlege. Lagrer og prøver igjen senere.", oppfolgingsplanId);
        } else {
            feiletSendingDAO.update(feiletSending);
            log.info("Fikk ikke sendt oppfolgingsplan med id {} til fastlege etter {} forsøk. Prøver igjen senere", oppfolgingsplanId, number_of_tries);
        }
    }

    public void fjernSendtOppfolgingsplan(Long oppfolgingsplanId) {
        feiletSendingDAO.remove(oppfolgingsplanId);
        log.info("Fikk sendt oppfolgingsplan med id {} etter nytt forsøk.", oppfolgingsplanId);
    }
}
