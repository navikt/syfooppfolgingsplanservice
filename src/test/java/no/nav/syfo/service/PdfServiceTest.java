package no.nav.syfo.service;

import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.DokumentDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static no.nav.syfo.testhelper.OppfolgingsplanTestUtilsKt.oppfolgingsplanGodkjentTvang;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class PdfServiceTest {

    @Mock
    private Metrikk metrikk;
    @Mock
    private DokumentDAO dokumentDAO;
    @InjectMocks
    private PdfService pdfService;

    @Test
    public void hentPdfTilAltinn() {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanGodkjentTvang();
        byte[] oppfoelgingsdialogPdf = hentOppfoelgingsdialogPdf();
        when(dokumentDAO.hent(any())).thenReturn(oppfoelgingsdialogPdf);

        byte[] dbPdf = pdfService.hentPdfTilAltinn(oppfolgingsplan);
        assertEquals(dbPdf, oppfoelgingsdialogPdf);
    }

    @Test(expected = RuntimeException.class)
    public void hentPdfTilAltinnUtenGodkjentPlan() {
        Oppfolgingsplan oppfolgingsplan = new Oppfolgingsplan();
        when(dokumentDAO.hent(anyString())).thenReturn(hentOppfoelgingsdialogPdf());

        pdfService.hentPdfTilAltinn(oppfolgingsplan);
    }

    private byte[] hentOppfoelgingsdialogPdf() {
        return new byte[2];
    }
}
