package no.nav.syfo.api.intern.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.service.DokumentService;
import no.nav.syfo.service.PdfService;
import org.junit.*;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnVeilederAzure;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.VEILEDER_ID;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

public class DokumentControllerTest extends AbstractRessursTilgangTest {

    @Inject
    private DokumentADController dokumentController;

    @MockBean
    private DokumentService dokumentService;
    @MockBean
    private GodkjentplanDAO godkjentplanDAO;
    @MockBean
    private PdfService pdfService;

    @Before
    public void setup() throws ParseException {
        loggInnVeilederAzure(oidcRequestContextHolder, VEILEDER_ID);
    }

    @After
    public void tearDown() {
        loggUtAlle(oidcRequestContextHolder);
    }

    @Test
    public void bilde_har_tilgang() throws IOException {
        mockSvarFraTilgangTilTjenestenViaAzure(OK);

        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(1)).thenReturn(Optional.of(new GodkjentPlan().dokumentUuid("1")));
        when(dokumentService.hentDokument("1")).thenReturn(new byte[]{});

        dokumentController.bilde(1L, 1);
    }

    @Test(expected = ForbiddenException.class)
    public void bilde_har_ikke_tilgang() throws IOException {
        mockSvarFraTilgangTilTjenestenViaAzure(FORBIDDEN);

        dokumentController.bilde(1L, 1);
    }

    @Test
    public void dokumentinfo_har_tilgang() {
        mockSvarFraTilgangTilTjenestenViaAzure(OK);

        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(1)).thenReturn(Optional.of(new GodkjentPlan().dokumentUuid("1")));
        when(dokumentService.hentDokument("1")).thenReturn(new byte[]{});

        dokumentController.dokumentinfo(1L);
    }

    @Test(expected = ForbiddenException.class)
    public void dokumentinfo_har_ikke_tilgang() {
        mockSvarFraTilgangTilTjenestenViaAzure(FORBIDDEN);

        dokumentController.dokumentinfo(1L);
    }

    @Test
    public void dokument_har_tilgang() {
        mockSvarFraTilgangTilTjenestenViaAzure(OK);

        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(1)).thenReturn(Optional.of(new GodkjentPlan().dokumentUuid("1")));
        when(dokumentService.hentDokument("1")).thenReturn(new byte[]{});

        dokumentController.dokument(1L);
    }

    @Test(expected = ForbiddenException.class)
    public void dokument_har_ikke_tilgang() {
        mockSvarFraTilgangTilTjenestenViaAzure(FORBIDDEN);

        dokumentController.dokument(1L);
    }

    @Test(expected = RuntimeException.class)
    public void bilde_annen_tilgangsfeil() throws IOException {
        mockSvarFraTilgangTilTjenestenViaAzure(INTERNAL_SERVER_ERROR);


        dokumentController.bilde(1L, 1);
    }

    @Test(expected = RuntimeException.class)
    public void dokument_annen_tilgangsfeil() {
        mockSvarFraTilgangTilTjenestenViaAzure(INTERNAL_SERVER_ERROR);

        dokumentController.dokument(1L);
    }

}
