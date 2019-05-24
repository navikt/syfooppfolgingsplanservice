package no.nav.syfo;

import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oppgave.oppfoelgingsdialog.JobbLoggSendOppfoelgingsdialogTilAltinn;
import no.nav.syfo.service.*;
import no.nav.syfo.util.OppfoelgingsdialogTestUtils;
import no.nav.syfo.util.Toggle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JobbLoggSendOppfoelgingsdialogTilAltinnTest {

    @Mock
    private OppfoelgingsdialogService oppfoelgingsdialogService;
    @Mock
    private JuridiskLoggService juridiskLoggService;
    @Mock
    private Metrikk metrikk;
    @Mock
    private Toggle toggle;
    @Mock
    private PdfService pdfService;
    @InjectMocks
    private JobbLoggSendOppfoelgingsdialogTilAltinn jobbLoggSendOppfoelgingsdialogTilAltinn;

    @Before
    public void setup() {
        when(toggle.toggleBatch()).thenReturn(true);
    }

    @Test
    public void skalUtfoereOppgaveArkiverOppgave() throws Exception {
        assertThat(jobbLoggSendOppfoelgingsdialogTilAltinn.skalUtforeOppgave(OPPFOELGINGSDIALOG_ARKIVER)).isTrue();
    }

    @Test
    public void skalUtfoereOppgaveIkkeArkiverOppgave() throws Exception {
        assertThat(jobbLoggSendOppfoelgingsdialogTilAltinn.skalUtforeOppgave(OPPFOELGINGSDIALOG_SEND)).isFalse();
    }

    @Test
    public void utfoerOppgaveArkiver() throws Exception {
        Oppfoelgingsdialog oppfoelgingsdialog = hentOppfoelgingsdialog();
        when(oppfoelgingsdialogService.hentGodkjentOppfoelgingsdialog(1L)).thenReturn(oppfoelgingsdialog);
        when(pdfService.hentPdfTilAltinn(oppfoelgingsdialog)).thenReturn(getOppfoelgingsdialogPdf());

        jobbLoggSendOppfoelgingsdialogTilAltinn.utfoerOppgave("1");

        verify(juridiskLoggService, times(1)).loggSendOppfoelgingsdialogTilAltinn(any());
    }

    private Oppfoelgingsdialog hentOppfoelgingsdialog() {
        return OppfoelgingsdialogTestUtils.oppfoelgingsdialogGodkjentTvang();
    }

    private byte[] getOppfoelgingsdialogPdf() {
        return new byte[2];
    }
}
