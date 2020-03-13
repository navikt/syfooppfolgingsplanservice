package no.nav.syfo;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oppgave.oppfoelgingsdialog.JobbSendOppfoelgingsdialogTilAltinn;
import no.nav.syfo.service.*;
import no.nav.syfo.util.OppfolgingsplanTestUtils;
import no.nav.syfo.ws.AltinnConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;
import static no.nav.syfo.util.OppfolgingsplanTestUtils.oppfolgingsplanGodkjentTvang;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JobbSendOppfolgingsplanTilAltinnTest {

    @Mock
    private Metrikk metrikk;
    @Mock
    private OppfolgingsplanService oppfolgingsplanService;
    @Mock
    private PdfService pdfService;
    @Mock
    private AltinnConsumer altinnConsumer;
    @Mock
    private AktorregisterConsumer aktorregisterConsumer;
    @InjectMocks
    private JobbSendOppfoelgingsdialogTilAltinn jobbSendOppfoelgingsdialogTilAltinn;

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
        Oppfolgingsplan oppfoelgingsplan = oppfolgingsplanGodkjentTvang();
        when(oppfolgingsplanService.hentGodkjentOppfolgingsplan(1L)).thenReturn(oppfolgingsplanGodkjentTvang());

        jobbSendOppfoelgingsdialogTilAltinn.utfoerOppgave("1");

        verify(altinnConsumer, times(1)).sendOppfolgingsplanTilArbeidsgiver(any());
    }

    private Oppfolgingsplan hentOppfoelgingsdialog() {
        return OppfolgingsplanTestUtils.oppfolgingsplanGodkjentTvang();
    }

    private byte[] hentOppfoelgingsdialogPdf() {
        return new byte[2];
    }
}
