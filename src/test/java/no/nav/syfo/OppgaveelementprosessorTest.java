package no.nav.syfo;

import no.nav.syfo.domain.AsynkOppgave;
import no.nav.syfo.oppgave.*;
import no.nav.syfo.oppgave.exceptions.OppgaveUgyldigTilstandException;
import no.nav.syfo.repository.dao.AsynkOppgaveDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static java.util.stream.Stream.of;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OppgaveelementprosessorTest {
    @Mock
    private List<Jobb> jobb;
    @Mock
    private AsynkOppgaveDAO asynkOppgaveDAO;
    @InjectMocks
    private Oppgaveelementprosessor oppgaveelementprosessor;

    private Jobb oppfoelgingsdialogSend = new Jobb() {
        @Override
        public void utfoerOppgave(String id) {

        }

        @Override
        public Oppgavetype oppgavetype() {
            return OPPFOELGINGSDIALOG_SEND;
        }
    };

    @Test
    public void runTransactional() throws Exception {
        when(jobb.stream()).thenReturn(of(oppfoelgingsdialogSend));

        oppgaveelementprosessor.runTransactional(
                new AsynkOppgave()
                        .id(1L)
                        .oppgavetype(OPPFOELGINGSDIALOG_SEND.name()));

        verify(asynkOppgaveDAO).delete(any());
    }

    @Test(expected = OppgaveUgyldigTilstandException.class)
    public void runTransactionalIngenJobberForOppgavetypen() throws Exception {
        when(jobb.stream()).thenReturn(of());

        oppgaveelementprosessor.runTransactional(
                new AsynkOppgave()
                        .id(1L)
                        .oppgavetype(OPPFOELGINGSDIALOG_SEND.name()));
    }

    @Test(expected = OppgaveUgyldigTilstandException.class)
    public void runTransactionalToJobberForOppgavetypen() throws Exception {
        when(jobb.stream()).thenReturn(of(oppfoelgingsdialogSend, oppfoelgingsdialogSend));

        oppgaveelementprosessor.runTransactional(
                new AsynkOppgave()
                        .id(1L)
                        .oppgavetype(OPPFOELGINGSDIALOG_SEND.name()));
    }
}
