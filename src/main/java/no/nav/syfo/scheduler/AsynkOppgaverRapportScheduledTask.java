package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.AsynkOppgave;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.AsynkOppgaveDAO;
import no.nav.syfo.util.Toggle;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class AsynkOppgaverRapportScheduledTask implements ScheduledTask {

    private AsynkOppgaveDAO asynkOppgaveDAO;

    @Inject
    private Metrikk metrikk;

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
