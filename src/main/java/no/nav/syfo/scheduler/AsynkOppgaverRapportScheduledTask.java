package no.nav.syfo.scheduler;

import no.nav.syfo.domain.AsynkOppgave;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.AsynkOppgaveDAO;
import no.nav.syfo.service.LeaderElectionService;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class AsynkOppgaverRapportScheduledTask {

    private static final Logger log = getLogger(AsynkOppgaverRapportScheduledTask.class);

    private AsynkOppgaveDAO asynkOppgaveDAO;

    @Inject
    private Metrikk metrikk;

    @Inject
    private LeaderElectionService leaderElectionService;

    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        if (leaderElectionService.isLeader()) {
            final List<AsynkOppgave> asynkOppgaver = asynkOppgaveDAO.finnOppgaver();
            log.info("Det finnes {} oppgaver på kø", asynkOppgaver.size());
            metrikk.tellHendelseMedAntall("oppgaverPaKo", asynkOppgaver.size());
        }
    }

    @Inject
    public void setAsynkOppgaveDAO(AsynkOppgaveDAO asynkOppgaveDAO) {
        this.asynkOppgaveDAO = asynkOppgaveDAO;
    }
}
