package no.nav.syfo.oppgave

import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oppgave.oppfoelgingsdialog.JobbSendOppfoelgingsdialogTilAltinn
import no.nav.syfo.service.OppfolgingsplanService
import no.nav.syfo.service.PdfService
import no.nav.syfo.testhelper.oppfolgingsplanGodkjentTvang
import no.nav.syfo.ws.AltinnConsumer
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class JobbSendOppfolgingsplanTilAltinnTest {
    @Mock
    private lateinit var metrikk: Metrikk

    @Mock
    private lateinit var oppfolgingsplanService: OppfolgingsplanService

    @Mock
    private lateinit var pdfService: PdfService

    @Mock
    private lateinit var altinnConsumer: AltinnConsumer

    @Mock
    private lateinit var aktorregisterConsumer: AktorregisterConsumer

    @InjectMocks
    private lateinit var jobbSendOppfoelgingsdialogTilAltinn: JobbSendOppfoelgingsdialogTilAltinn

    @Test
    @Throws(Exception::class)
    fun utfoerOppgaveSendOppfoelgingsdialogOppgave() {
        Assertions.assertThat(jobbSendOppfoelgingsdialogTilAltinn.skalUtforeOppgave(Oppgavetype.OPPFOELGINGSDIALOG_SEND)).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun utfoerOppgaveIkkeSendOppfoelgingsdialogOppgave() {
        Assertions.assertThat(jobbSendOppfoelgingsdialogTilAltinn.skalUtforeOppgave(Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER)).isFalse()
    }

    @Test
    @Throws(Exception::class)
    fun utfoerOppgaveSendOppfoelgingsdialog() {
        Mockito.`when`(oppfolgingsplanService.hentGodkjentOppfolgingsplan(1L)).thenReturn(oppfolgingsplanGodkjentTvang())
        jobbSendOppfoelgingsdialogTilAltinn.utfoerOppgave("1")
        Mockito.verify(altinnConsumer, Mockito.times(1)).sendOppfolgingsplanTilArbeidsgiver(ArgumentMatchers.any())
    }
}
