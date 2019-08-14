package no.nav.syfo.scheduler;

import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oppgave.Oppgavelisteprosessor;
import no.nav.syfo.service.LeaderElectionService;
import no.nav.syfo.util.Toggle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AsynkOppgaverScheduledTaskTest {

    @Mock
    private Toggle toggle;
    @Mock
    private Oppgavelisteprosessor oppgavelisteprosessor;
    @Mock
    private LeaderElectionService leaderElectionService;
    @Mock
    private Metrikk metrikk;

    @InjectMocks
    private AsynkOppgaverScheduledTask asynkOppgaverScheduledTask;

    @Before
    public void setup() {
        when(toggle.toggleBatch()).thenReturn(true);
        when(leaderElectionService.isLeader()).thenReturn(true);
    }

    @Test
    public void runOppgavelisteprosessor() throws Exception {
        asynkOppgaverScheduledTask.run();

        verify(oppgavelisteprosessor).run();
        verify(metrikk).tellHendelse("kanskje_AsynkOppgave");
        verify(metrikk).tellHendelse("kjorer_AsynkOppgave");
    }
}
