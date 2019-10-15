package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.PdfService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
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
    private static Long sideId = 1L;

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

    @Test
    public void hentSidebilde_som_bruker() {
        byte[] pdf = new byte[10];
        int sideantall = 3;
        when(pdfService.hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(pdf);
        when(pdfService.hentAntallSiderIDokument(pdf)).thenReturn(sideantall);
        ResponseEntity response = ResponseEntity.badRequest().build();
        try {
            when(pdfService.pdf2image(pdf, sideId.intValue())).thenReturn(pdf);
            response = dokumentController.hentSidebilde(oppfolgingsplanId, sideId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        verify(metrikk).tellHendelse("hent_sidebilde");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(pdf, response.getBody());
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_hentSidebilde() {
        loggUtAlle(oidcRequestContextHolder);

        try {
            dokumentController.hentSidebilde(oppfolgingsplanId, sideId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void hentPdfurler_som_bruker() {
        byte[] pdf = new byte[10];
        int sideantall = 3;
        when(pdfService.hentPdf(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(pdf);
        when(pdfService.hentAntallSiderIDokument(pdf)).thenReturn(sideantall);

        List<String> returnertListe = dokumentController.hentPdfurler(oppfolgingsplanId);

        String forventetUrl = "https://syfoapi.nav.no/syfooppfolgingsplanservice/api/dokument/" + oppfolgingsplanId + "/side/";

        verify(metrikk).tellHendelse("hent_pdfurler");

        assertEquals(sideantall, returnertListe.size());

        for (int i = 0; i < sideantall; i++) {
            assertEquals(forventetUrl + (i + 1), returnertListe.get(i));
        }
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_hentPdfurler() {
        loggUtAlle(oidcRequestContextHolder);

        dokumentController.hentPdfurler(oppfolgingsplanId);
    }

    @Test
    public void hentSyfoapiUrl_default() {
        String returnertVerdi = dokumentController.hentSyfoapiUrl("");

        String forventetVerdi = "https://syfoapi.nav.no";

        assertEquals(forventetVerdi, returnertVerdi);
    }

    @Test
    public void hentSyfoapiUrl_prod() {
        String returnertVerdi = dokumentController.hentSyfoapiUrl("prod-fss");

        String forventetVerdi = "https://syfoapi.nav.no";

        assertEquals(forventetVerdi, returnertVerdi);
    }

    @Test
    public void hentSyfoapiUrl_preprod() {
        String returnertVerdi = dokumentController.hentSyfoapiUrl("dev-fss");

        String forventetVerdi = "https://syfoapi-q.nav.no";

        assertEquals(forventetVerdi, returnertVerdi);
    }
}
