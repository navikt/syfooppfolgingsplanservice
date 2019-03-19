package no.nav.syfo.services;

import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.DokumentDAO;
import no.nav.syfo.service.PdfService;
import no.nav.syfo.util.OppfoelgingsdialogTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

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
        Oppfoelgingsdialog oppfoelgingsdialog = OppfoelgingsdialogTestUtils.oppfoelgingsdialogGodkjentTvang();
        byte[] oppfoelgingsdialogPdf = hentOppfoelgingsdialogPdf();
        when(dokumentDAO.hent(any())).thenReturn(oppfoelgingsdialogPdf);

        byte[] dbPdf = pdfService.hentPdfTilAltinn(oppfoelgingsdialog);
        assertEquals(dbPdf, oppfoelgingsdialogPdf);
    }

    @Test(expected = RuntimeException.class)
    public void hentPdfTilAltinnUtenGodkjentPlan() {
        Oppfoelgingsdialog oppfoelgingsdialog = new Oppfoelgingsdialog();
        when(dokumentDAO.hent(anyString())).thenReturn(hentOppfoelgingsdialogPdf());

        pdfService.hentPdfTilAltinn(oppfoelgingsdialog);
    }

    private byte[] hentOppfoelgingsdialogPdf() {
        return new byte[2];
    }
}
