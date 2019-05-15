package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.oppgave.Oppgavelisteprosessor;
import no.nav.syfo.util.Toggle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Slf4j
@Component
public class AsynkOppgaverScheduledTask {

    private Oppgavelisteprosessor oppgavelisteprosessor;
    @Inject
    private Toggle toggle;

    @Scheduled(cron = "*/10 * * * * *")
    public void run() {
        if (toggle.toggleBatch()) {
            log.info("TRACEBATCH: run {}", this.getClass().getName());

            oppgavelisteprosessor.run();
        }
    }

    @Inject
    public void setOppgavelisteprosessor(Oppgavelisteprosessor oppgavelisteprosessor) {
        this.oppgavelisteprosessor = oppgavelisteprosessor;
    }
}
