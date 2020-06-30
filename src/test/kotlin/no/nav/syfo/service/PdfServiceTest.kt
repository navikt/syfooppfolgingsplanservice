package no.nav.syfo.service

import no.nav.syfo.domain.Oppfolgingsplan
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.repository.dao.DokumentDAO
import no.nav.syfo.testhelper.oppfolgingsplanGodkjentTvang
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class PdfServiceTest {
    @Mock
    private lateinit var metrikk: Metrikk

    @Mock
    private lateinit var dokumentDAO: DokumentDAO

    @InjectMocks
    private lateinit var pdfService: PdfService

    @Test
    fun hentPdfTilAltinn() {
        val oppfolgingsplan = oppfolgingsplanGodkjentTvang()
        val oppfoelgingsdialogPdf = hentOppfoelgingsdialogPdf()
        Mockito.`when`(dokumentDAO.hent(ArgumentMatchers.any())).thenReturn(oppfoelgingsdialogPdf)
        val dbPdf = pdfService.hentPdfTilAltinn(oppfolgingsplan)
        Assert.assertEquals(dbPdf, oppfoelgingsdialogPdf)
    }

    @Test(expected = RuntimeException::class)
    fun hentPdfTilAltinnUtenGodkjentPlan() {
        val oppfolgingsplan = Oppfolgingsplan()
        Mockito.`when`(dokumentDAO.hent(ArgumentMatchers.anyString())).thenReturn(hentOppfoelgingsdialogPdf())
        pdfService.hentPdfTilAltinn(oppfolgingsplan)
    }

    private fun hentOppfoelgingsdialogPdf(): ByteArray {
        return ByteArray(2)
    }
}
