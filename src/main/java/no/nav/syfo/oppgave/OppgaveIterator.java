package no.nav.syfo.oppgave;

import no.nav.syfo.domain.AsynkOppgave;
import no.nav.syfo.repository.dao.AsynkOppgaveDAO;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Iterator;

@Component
public class OppgaveIterator implements Iterator<AsynkOppgave> {

    private AsynkOppgaveDAO asynkOppgaveDAO;

    public OppgaveIterator() {
    }

    @Override
    public boolean hasNext() {
        return asynkOppgaveDAO.finnFoersteOppgaveUtenAvhengighet().isPresent();
    }

    @Override
    public AsynkOppgave next() {
        return asynkOppgaveDAO.finnFoersteOppgaveUtenAvhengighet().orElseThrow(() -> new NullPointerException("Asynk Oppgaveiterator har neste oppgave er true, men optional er tom"));
    }

    @Inject
    public void setAsynkOppgaveDAO(AsynkOppgaveDAO asynkOppgaveDAO) {
        this.asynkOppgaveDAO = asynkOppgaveDAO;
    }
}
