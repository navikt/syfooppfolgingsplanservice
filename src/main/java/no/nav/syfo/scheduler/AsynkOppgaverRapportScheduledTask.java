package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.AsynkOppgave;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.AsynkOppgaveDAO;
import no.nav.syfo.service.LeaderElectionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@Component
public class AsynkOppgaverRapportScheduledTask {

    private AsynkOppgaveDAO asynkOppgaveDAO;

    @Inject
    private Metrikk metrikk;

    @Inject
    private LeaderElectionService leaderElectionService;

    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        if (leaderElectionService.isLeader()) {
            log.info("TRACEBATCH: run {}", this.getClass().getName());

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
