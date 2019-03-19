package no.nav.syfo.oppgave;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.AsynkOppgaveDAO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static no.nav.syfo.util.ToggleUtil.kjorerIProduksjon;

@Slf4j
@Service
public class Oppgavelisteprosessor {

    private Oppgaveelementprosessor oppgaveelementprosessor;
    private AsynkOppgaveDAO asynkOppgaveDAO;
    private OppgaveIterator oppgaveIterator;
    @Inject
    private Metrikk metrikk;

    private final int limit = 100;

    public void run() {
        log.info("Kjører asynk oppgaver");

        StreamSupport.stream(Spliterators.spliteratorUnknownSize(oppgaveIterator, Spliterator.ORDERED), false)
                .limit(limit)
                .forEach(oppgave -> {
                    try {
                        oppgaveelementprosessor.runTransactional(oppgave);
                        metrikk.tellAsynkOppgave(oppgave, true);

                    } catch (Exception e) {
                        log.error("Feil ved prossesering av oppgavetype " + oppgave.oppgavetype + " med id " + oppgave.id + " og ressursId " + oppgave.ressursId, e);
                        asynkOppgaveDAO.update(oppgave.inkrementerAntallForsoek());
                        metrikk.tellAsynkOppgave(oppgave, false);

                        // I test: sletter asynkOppgave som har blitt forsøkt prosessert minst 100 ganger
                        if (!kjorerIProduksjon() && oppgave.antallForsoek > 100) {
                            log.info("Oppgave " + oppgave.oppgavetype + " med id " + oppgave.id + " og ressursId " + oppgave.ressursId +
                                    " har feilet " + (oppgave.antallForsoek - 1) + " ganger. Sletter oppgaven fra databasen");
                            asynkOppgaveDAO.delete(oppgave.id);
                        }
                    }
                });
    }

    @Inject
    public void setOppgaveelementprosessor(Oppgaveelementprosessor oppgaveelementprosessor) {
        this.oppgaveelementprosessor = oppgaveelementprosessor;
    }

    @Inject
    public void setAsynkOppgaveDAO(AsynkOppgaveDAO asynkOppgaveDAO) {
        this.asynkOppgaveDAO = asynkOppgaveDAO;
    }

    @Inject
    public void setOppgaveIterator(OppgaveIterator oppgaveIterator) {
        this.oppgaveIterator = oppgaveIterator;
    }
}
