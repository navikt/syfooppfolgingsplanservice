package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.oppgave.Oppgavelisteprosessor;
import no.nav.syfo.util.Toggle;
import org.slf4j.Logger;

import javax.inject.Inject;

@Slf4j
public class AsynkOppgaverScheduledTask implements ScheduledTask {

    private Oppgavelisteprosessor oppgavelisteprosessor;
    @Inject
    private Toggle toggle;

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
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
