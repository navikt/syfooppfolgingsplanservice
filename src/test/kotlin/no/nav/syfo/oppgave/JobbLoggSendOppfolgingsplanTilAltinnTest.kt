package no.nav.syfo.oppgave

import no.nav.syfo.domain.Oppfolgingsplan
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oppgave.oppfoelgingsdialog.JobbLoggSendOppfoelgingsdialogTilAltinn
import no.nav.syfo.service.*
import no.nav.syfo.testhelper.oppfolgingsplanGodkjentTvang
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class JobbLoggSendOppfolgingsplanTilAltinnTest {
    @Mock
    private lateinit var oppfolgingsplanService: OppfolgingsplanService

    @Mock
    private lateinit var juridiskLoggService: JuridiskLoggService

    @Mock
    private lateinit var metrikk: Metrikk

    @Mock
    private lateinit var pdfService: PdfService

    @InjectMocks
    private lateinit var jobbLoggSendOppfoelgingsdialogTilAltinn: JobbLoggSendOppfoelgingsdialogTilAltinn

    @Test
    @Throws(Exception::class)
    fun skalUtfoereOppgaveArkiverOppgave() {
        Assertions.assertThat(jobbLoggSendOppfoelgingsdialogTilAltinn.skalUtforeOppgave(Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER)).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun skalUtfoereOppgaveIkkeArkiverOppgave() {
        Assertions.assertThat(jobbLoggSendOppfoelgingsdialogTilAltinn.skalUtforeOppgave(Oppgavetype.OPPFOELGINGSDIALOG_SEND)).isFalse()
    }

    @Test
    @Throws(Exception::class)
    fun utfoerOppgaveArkiver() {
        val oppfolgingsplan = hentOppfoelgingsdialog()
        Mockito.`when`(oppfolgingsplanService.hentGodkjentOppfolgingsplan(1L)).thenReturn(oppfolgingsplan)
        Mockito.`when`(pdfService.hentPdfTilAltinn(oppfolgingsplan)).thenReturn(oppfoelgingsdialogPdf)
        jobbLoggSendOppfoelgingsdialogTilAltinn.utfoerOppgave("1")
        Mockito.verify(juridiskLoggService, Mockito.times(1)).loggSendOppfoelgingsdialogTilAltinn(ArgumentMatchers.any())
    }

    private fun hentOppfoelgingsdialog(): Oppfolgingsplan {
        return oppfolgingsplanGodkjentTvang()
    }

    private val oppfoelgingsdialogPdf: ByteArray
        get() = ByteArray(2)
}
