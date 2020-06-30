package no.nav.syfo.oppgave;

import no.nav.syfo.LocalApplication;
import no.nav.syfo.domain.AsynkOppgave;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oppgave.*;
import no.nav.syfo.oppgave.exceptions.OppgaveFinnerIkkeElementException;
import no.nav.syfo.repository.dao.AsynkOppgaveDAO;
import no.nav.syfo.util.Toggle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class OppgavelisteprosessorTest {

    @Mock
    private Oppgaveelementprosessor oppgaveelementprosessor;
    @Mock
    private AsynkOppgaveDAO asynkOppgaveDAO;
    @Mock
    private OppgaveIterator oppgaveIterator;
    @Mock
    private Metrikk metrikk;
    @Mock
    private Toggle toggle;

    @InjectMocks
    private Oppgavelisteprosessor oppgavelisteprosessor;

    @Before
    public void setup() {
        when(toggle.erPreprod()).thenReturn(true);
    }

    @Test
    public void runIngenOppgaver() throws Exception {
        when(asynkOppgaveDAO.finnOppgaver()).thenReturn(emptyList());

        oppgavelisteprosessor.run();

        verify(oppgaveelementprosessor, never()).runTransactional(any(AsynkOppgave.class));
        verify(asynkOppgaveDAO, never()).create(any(AsynkOppgave.class));
    }

    @Test
    public void runEnOppgave() throws Exception {
        when(oppgaveIterator.hasNext()).thenReturn(true, false);
        when(oppgaveIterator.next()).thenReturn(new AsynkOppgave()
                .id(1L)
                .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name()));

        oppgavelisteprosessor.run();

        verify(oppgaveelementprosessor, times(1)).runTransactional(any(AsynkOppgave.class));
        verify(asynkOppgaveDAO, never()).create(any(AsynkOppgave.class));
    }

    @Test
    public void runEnOppgaveFeilIProsessering() throws Exception {

        final AsynkOppgave oppgave = new AsynkOppgave()
                .id(1L)
                .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name());

        when(oppgaveIterator.hasNext()).thenReturn(true, false);
        when(oppgaveIterator.next()).thenReturn(oppgave);

        doThrow(OppgaveFinnerIkkeElementException.class).when(oppgaveelementprosessor).runTransactional(oppgave);

        oppgavelisteprosessor.run();

        verify(oppgaveelementprosessor).runTransactional(any(AsynkOppgave.class));

        ArgumentCaptor<AsynkOppgave> captor = ArgumentCaptor.forClass(AsynkOppgave.class);
        verify(asynkOppgaveDAO).update(captor.capture());

        assertThat(captor.getValue().id).isEqualTo(1L);
        assertThat(captor.getValue().antallForsoek).isEqualTo(1);
    }

    @Test
    public void runToOppgaverFeilIProsesseringAv1() throws Exception {

        final AsynkOppgave oppgave1 = new AsynkOppgave()
                .id(1L)
                .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name());
        final AsynkOppgave oppgave2 = new AsynkOppgave()
                .id(2L)
                .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name());

        when(oppgaveIterator.hasNext()).thenReturn(true, true, false);
        when(oppgaveIterator.next()).thenReturn(oppgave1, oppgave2);

        doThrow(OppgaveFinnerIkkeElementException.class).when(oppgaveelementprosessor).runTransactional(oppgave1);

        oppgavelisteprosessor.run();

        verify(oppgaveelementprosessor, times(2)).runTransactional(any(AsynkOppgave.class));

        ArgumentCaptor<AsynkOppgave> captor = ArgumentCaptor.forClass(AsynkOppgave.class);
        verify(asynkOppgaveDAO).update(captor.capture());

        assertThat(captor.getValue().id).isEqualTo(1L);
        assertThat(captor.getValue().antallForsoek).isEqualTo(1);
    }

    @Test
    public void utfoererMaksLimitOppgaverIEnIterasjon() {
        final AsynkOppgave oppgave1 = new AsynkOppgave()
                .id(1L)
                .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name());

        when(oppgaveIterator.hasNext()).thenReturn(true);
        when(oppgaveIterator.next()).thenReturn(oppgave1);

        oppgavelisteprosessor.run();

        verify(oppgaveelementprosessor, times(100)).runTransactional(any(AsynkOppgave.class));
    }

    @Test
    public void sletterOppgaverSomHarFeilet100ganger() {
        final AsynkOppgave oppgave1 = new AsynkOppgave()
                .id(1L)
                .oppgavetype(OPPFOELGINGSDIALOG_SEND.name())
                .antallForsoek(100)
                .opprettetTidspunkt(now().minusMinutes(500));

        when(oppgaveIterator.hasNext()).thenReturn(true, false);
        when(oppgaveIterator.next()).thenReturn(oppgave1);

        doThrow(OppgaveFinnerIkkeElementException.class).when(oppgaveelementprosessor).runTransactional(oppgave1);

        oppgavelisteprosessor.run();

        verify(asynkOppgaveDAO, times(1)).delete(any());
    }

    @Test
    public void sletterIkkeOppgaverHvisViKjorerIProduksjon() {
        when(toggle.erPreprod()).thenReturn(false);

        final AsynkOppgave oppgave1 = new AsynkOppgave()
                .id(1L)
                .oppgavetype(OPPFOELGINGSDIALOG_SEND.name())
                .antallForsoek(100)
                .opprettetTidspunkt(now().minusMinutes(500));

        when(oppgaveIterator.hasNext()).thenReturn(true, false);
        when(oppgaveIterator.next()).thenReturn(oppgave1);

        doThrow(OppgaveFinnerIkkeElementException.class).when(oppgaveelementprosessor).runTransactional(oppgave1);

        oppgavelisteprosessor.run();

        verify(asynkOppgaveDAO, never()).delete(any());
    }

    @Test
    public void sletterIkkeOppgaverSomHarFeiletMindreEnn100Ganger() {
        final AsynkOppgave oppgave1 = new AsynkOppgave()
                .id(1L)
                .oppgavetype(OPPFOELGINGSDIALOG_SEND.name())
                .antallForsoek(10)
                .opprettetTidspunkt(now().minusMinutes(500));

        when(oppgaveIterator.hasNext()).thenReturn(true, false);
        when(oppgaveIterator.next()).thenReturn(oppgave1);

        doThrow(OppgaveFinnerIkkeElementException.class).when(oppgaveelementprosessor).runTransactional(oppgave1);

        oppgavelisteprosessor.run();

        verify(asynkOppgaveDAO, never()).delete(any());
    }

}
