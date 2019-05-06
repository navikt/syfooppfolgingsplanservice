package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.PdfService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import javax.inject.Inject;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DokumentControllerTest extends AbstractRessursTilgangTest {

    @Inject
    private DokumentController dokumentController;

    @MockBean
    PdfService pdfService;
    @MockBean
    Metrikk metrikk;

    private static Long oppfolgingsplanId = 1L;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);
    }

    @Test
    public void hent_pdf_som_bruker() {
        byte[] pdf = new byte[10];
        when(pdfService.hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(pdf);

        ResponseEntity response = dokumentController.hentPdf(oppfolgingsplanId);
        verify(pdfService).hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(pdf, response.getBody());
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_hent_pdf() {
        loggUtAlle(oidcRequestContextHolder);

        dokumentController.hentPdf(oppfolgingsplanId);
    }
}