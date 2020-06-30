package no.nav.syfo;

import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oppgave.oppfoelgingsdialog.JobbLoggSendOppfoelgingsdialogTilAltinn;
import no.nav.syfo.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;
import static no.nav.syfo.testhelper.OppfolgingsplanTestUtilsKt.oppfolgingsplanGodkjentTvang;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JobbLoggSendOppfolgingsplanTilAltinnTest {

    @Mock
    private OppfolgingsplanService oppfolgingsplanService;
    @Mock
    private JuridiskLoggService juridiskLoggService;
    @Mock
    private Metrikk metrikk;
    @Mock
    private PdfService pdfService;
    @InjectMocks
    private JobbLoggSendOppfoelgingsdialogTilAltinn jobbLoggSendOppfoelgingsdialogTilAltinn;

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
        Oppfolgingsplan oppfolgingsplan = hentOppfoelgingsdialog();
        when(oppfolgingsplanService.hentGodkjentOppfolgingsplan(1L)).thenReturn(oppfolgingsplan);
        when(pdfService.hentPdfTilAltinn(oppfolgingsplan)).thenReturn(getOppfoelgingsdialogPdf());

        jobbLoggSendOppfoelgingsdialogTilAltinn.utfoerOppgave("1");

        verify(juridiskLoggService, times(1)).loggSendOppfoelgingsdialogTilAltinn(any());
    }

    private Oppfolgingsplan hentOppfoelgingsdialog() {
        return oppfolgingsplanGodkjentTvang();
    }

    private byte[] getOppfoelgingsdialogPdf() {
        return new byte[2];
    }
}
