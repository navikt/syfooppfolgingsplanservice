package no.nav.syfo.oppgave;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.AsynkOppgave;
import no.nav.syfo.oppgave.exceptions.OppgaveUgyldigTilstandException;
import no.nav.syfo.repository.dao.AsynkOppgaveDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static no.nav.syfo.oppgave.Oppgavetype.valueOf;

@Slf4j
@Service
public class Oppgaveelementprosessor {

    private List<Jobb> jobber;
    private AsynkOppgaveDAO asynkOppgaveDAO;

    @Transactional
    public void runTransactional(AsynkOppgave asynkOppgave) {
        Oppgavetype oppgavetype = valueOf(asynkOppgave.oppgavetype);

        long oppgaveFerdig = jobber
                .stream()
                .filter(jobb -> jobb.skalUtforeOppgave(oppgavetype))
                .peek(jobb -> jobb.utfoerOppgave(asynkOppgave.ressursId))
                .count();

        if (oppgaveFerdig == 1) {
            log.info("Oppgave {} med id {} og ressursId {} er utført etter {} forsøk", oppgavetype, asynkOppgave.id, asynkOppgave.ressursId, asynkOppgave.antallForsoek + 1);
            asynkOppgaveDAO.delete(asynkOppgave.id);
        } else {
            log.error(oppgaveFerdig + " jobber har gjort en jobb på oppgavetypen " + oppgavetype);
            throw new OppgaveUgyldigTilstandException(oppgaveFerdig + " jobber har gjort en jobb på oppgavetypen " + oppgavetype);
        }
    }

    @Inject
    public void setJobber(List<Jobb> jobber) {
        this.jobber = jobber;
    }

    @Inject
    public void setAsynkOppgaveDAO(AsynkOppgaveDAO asynkOppgaveDAO) {
        this.asynkOppgaveDAO = asynkOppgaveDAO;
    }
}
