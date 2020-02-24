package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oppgave.Oppgavelisteprosessor;
import no.nav.syfo.service.LeaderElectionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Slf4j
@Component
public class AsynkOppgaverScheduledTask {

    private Oppgavelisteprosessor oppgavelisteprosessor;

    @Inject
    private LeaderElectionService leaderElectionService;

    @Inject
    private Metrikk metrikk;

    @Scheduled(fixedRate = 2000)
    public void run() {
        metrikk.tellHendelse("kanskje_AsynkOppgave");
        if (leaderElectionService.isLeader()) {
            metrikk.tellHendelse("kjorer_AsynkOppgave");
            oppgavelisteprosessor.run();
        }
    }

    @Inject
    public void setOppgavelisteprosessor(Oppgavelisteprosessor oppgavelisteprosessor) {
        this.oppgavelisteprosessor = oppgavelisteprosessor;
    }
}
