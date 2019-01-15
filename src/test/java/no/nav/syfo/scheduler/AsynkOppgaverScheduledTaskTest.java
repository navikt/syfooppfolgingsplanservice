package no.nav.syfo.scheduler;

import no.nav.syfo.oppgave.Oppgavelisteprosessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.setProperty;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static no.nav.syfo.util.ToggleUtil.TOGGLE_ENABLE_BATCH;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AsynkOppgaverScheduledTaskTest {

    @Mock
    private Oppgavelisteprosessor oppgavelisteprosessor;

    @InjectMocks
    private AsynkOppgaverScheduledTask asynkOppgaverScheduledTask;

    @Before
    public void setup() {
        setProperty(LOCAL_MOCK, "false");
        setProperty(TOGGLE_ENABLE_BATCH, "true");
    }

    @After
    public void cleanUp() {
        setProperty(LOCAL_MOCK, "");
        setProperty(TOGGLE_ENABLE_BATCH, "");
    }

    @Test
    public void runOppgavelisteprosessor() throws Exception {
        asynkOppgaverScheduledTask.run();

        verify(oppgavelisteprosessor).run();
    }
}
