package no.nav.syfo.api.intern.ressurs;

import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.service.DokumentService;
import no.nav.syfo.service.PdfService;
import org.glassfish.jersey.message.internal.Statuses;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientBuilder.class)
public class DokumentRessursTest extends AbstractRessursTilgangTest{

    @Mock
    private DokumentService dokumentService;
    @Mock
    private PdfService pdfService;
    @Mock
    private GodkjentplanDAO godkjentplanDAO;

    @InjectMocks
    private DokumentRessurs dokumentRessurs;

    @Test
    public void bilde_har_tilgang() throws IOException {
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(1)).thenReturn(Optional.of(new GodkjentPlan().dokumentUuid("1")));
        when(dokumentService.hentDokument("1")).thenReturn(new byte[]{});

        dokumentRessurs.bilde(1L, 1);

        Mockito.verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void bilde_har_ikke_tilgang() throws IOException {
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        dokumentRessurs.bilde(1L, 1);
    }

    @Test(expected = WebApplicationException.class)
    public void bilde_annen_tilgangsfeil() throws IOException {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(Statuses.from(500, "Sand i olja"));

        dokumentRessurs.bilde(1L, 1);
    }

    @Test
    public void dokumentinfo_har_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(1)).thenReturn(Optional.of(new GodkjentPlan().dokumentUuid("1")));
        when(dokumentService.hentDokument("1")).thenReturn(new byte[]{});

        dokumentRessurs.dokumentinfo(1L);

        Mockito.verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void dokumentinfo_har_ikke_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        dokumentRessurs.dokumentinfo(1L);
    }

    @Test(expected = WebApplicationException.class)
    public void dokumentinfo_annen_tilgangsfeil() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(Statuses.from(500, "Is i rubben"));


        dokumentRessurs.dokumentinfo(1L);
    }

    @Test
    public void dokument_har_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(1)).thenReturn(Optional.of(new GodkjentPlan().dokumentUuid("1")));
        when(dokumentService.hentDokument("1")).thenReturn(new byte[]{});

        dokumentRessurs.dokument(1L);

        Mockito.verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void dokument_har_ikke_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        dokumentRessurs.dokument(1L);
    }

    @Test(expected = WebApplicationException.class)
    public void dokument_annen_tilgangsfeil() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(Statuses.from(500, "Rotte i innsuget"));


        dokumentRessurs.dokument(1L);
    }

}
