package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.oppgave.Oppgavelisteprosessor;
import org.slf4j.Logger;

import javax.inject.Inject;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static no.nav.syfo.util.ToggleUtil.toggleBatch;

@Slf4j
public class AsynkOppgaverScheduledTask implements ScheduledTask {

    private Oppgavelisteprosessor oppgavelisteprosessor;

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public void run() {
        if (!"true".equals(getProperty(LOCAL_MOCK)) && toggleBatch()) {
            log.info("TRACEBATCH: run {}", this.getClass().getName());

            oppgavelisteprosessor.run();
        }
    }

    @Inject
    public void setOppgavelisteprosessor(Oppgavelisteprosessor oppgavelisteprosessor) {
        this.oppgavelisteprosessor = oppgavelisteprosessor;
    }
}
