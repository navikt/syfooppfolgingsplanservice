package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.metrics.Event;
import no.nav.syfo.domain.AsynkOppgave;
import no.nav.syfo.repository.dao.AsynkOppgaveDAO;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;

import static java.lang.System.getProperty;
import static no.nav.metrics.MetricsFactory.createEvent;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static no.nav.syfo.util.ToggleUtil.toggleBatch;

@Slf4j
public class AsynkOppgaverRapportScheduledTask implements ScheduledTask {

    private AsynkOppgaveDAO asynkOppgaveDAO;

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public void run() {
        if (!"true".equals(getProperty(LOCAL_MOCK)) && toggleBatch()) {
            log.info("TRACEBATCH: run {}", this.getClass().getName());

            final List<AsynkOppgave> asynkOppgaver = asynkOppgaveDAO.finnOppgaver();
            log.info("Det finnes {} oppgaver på kø", asynkOppgaver.size());

            Event hendelse = createEvent("asynkOppgaverapport");
            hendelse.addFieldToReport("oppgaverPaKo", asynkOppgaver.size());
            hendelse.report();
        }
    }

    @Inject
    public void setAsynkOppgaveDAO(AsynkOppgaveDAO asynkOppgaveDAO) {
        this.asynkOppgaveDAO = asynkOppgaveDAO;
    }
}
