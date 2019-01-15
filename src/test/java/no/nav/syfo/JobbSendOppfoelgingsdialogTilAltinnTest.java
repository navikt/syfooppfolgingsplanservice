package no.nav.syfo;

import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.oppgave.oppfoelgingsdialog.JobbSendOppfoelgingsdialogTilAltinn;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.OppfoelgingsdialogService;
import no.nav.syfo.service.PdfService;
import no.nav.syfo.util.OppfoelgingsdialogTestUtils;
import no.nav.syfo.ws.AltinnConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.setProperty;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;
import static no.nav.syfo.util.OppfoelgingsdialogTestUtils.oppfoelgingsdialogGodkjentTvang;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static no.nav.syfo.util.ToggleUtil.TOGGLE_ENABLE_BATCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JobbSendOppfoelgingsdialogTilAltinnTest {

    @Mock
    private OppfoelgingsdialogService oppfoelgingsdialogService;
    @Mock
    private PdfService pdfService;
    @Mock
    private AltinnConsumer altinnConsumer;
    @Mock
    private AktoerService aktoerService;
    @InjectMocks
    private JobbSendOppfoelgingsdialogTilAltinn jobbSendOppfoelgingsdialogTilAltinn;

    @Before
    public void setup() {
        setProperty(LOCAL_MOCK, "false");
        setProperty(TOGGLE_ENABLE_BATCH, "true");
    }

    @Test
    public void utfoerOppgaveSendOppfoelgingsdialogOppgave() throws Exception {
        assertThat(jobbSendOppfoelgingsdialogTilAltinn.skalUtforeOppgave(OPPFOELGINGSDIALOG_SEND)).isTrue();
    }

    @Test
    public void utfoerOppgaveIkkeSendOppfoelgingsdialogOppgave() throws Exception {
        assertThat(jobbSendOppfoelgingsdialogTilAltinn.skalUtforeOppgave(OPPFOELGINGSDIALOG_ARKIVER)).isFalse();
    }

    @Test
    public void utfoerOppgaveSendOppfoelgingsdialog() throws Exception {
        Oppfoelgingsdialog oppfoelgingsdialog = hentOppfoelgingsdialog();
        when(oppfoelgingsdialogService.hentGodkjentOppfoelgingsdialog(1L)).thenReturn(oppfoelgingsdialogGodkjentTvang());
        when(aktoerService.hentFnrForAktoer("aktoerId")).thenReturn("aktoerFnr");
        when(pdfService.hentPdfTilAltinn(oppfoelgingsdialog)).thenReturn(hentOppfoelgingsdialogPdf());

        jobbSendOppfoelgingsdialogTilAltinn.utfoerOppgave("1");

        verify(altinnConsumer, times(1)).sendOppfoelgingsplanTilArbeidsgiver(any());
    }

    private Oppfoelgingsdialog hentOppfoelgingsdialog() {
        return OppfoelgingsdialogTestUtils.oppfoelgingsdialogGodkjentTvang();
    }

    private byte[] hentOppfoelgingsdialogPdf() {
        return new byte[2];
    }
}
