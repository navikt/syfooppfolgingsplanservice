package no.nav.syfo.scheduler;

import no.nav.syfo.oppgave.Oppgavelisteprosessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static no.nav.syfo.util.ToggleUtil.toggleBatch;

public class AsynkOppgaverScheduledTask implements ScheduledTask {

    public static final Logger LOG = LoggerFactory.getLogger(AsynkOppgaverScheduledTask.class);

    private Oppgavelisteprosessor oppgavelisteprosessor;

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public void run() {
        if (!"true".equals(getProperty(LOCAL_MOCK)) && toggleBatch()) {
            LOG.info("TRACEBATCH: run {}", this.getClass().getName());

            oppgavelisteprosessor.run();
        }
    }

    @Inject
    public void setOppgavelisteprosessor(Oppgavelisteprosessor oppgavelisteprosessor) {
        this.oppgavelisteprosessor = oppgavelisteprosessor;
    }
}
