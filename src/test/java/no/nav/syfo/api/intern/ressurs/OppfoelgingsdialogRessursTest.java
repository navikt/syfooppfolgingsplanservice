package no.nav.syfo.api.intern.ressurs;

import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.BrukerprofilService;
import no.nav.syfo.service.OrganisasjonService;
import no.nav.syfo.service.VeilederOppgaverService;
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

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientBuilder.class)
public class OppfoelgingsdialogRessursTest extends AbstractRessursTilgangTest{

    @Mock
    private AktoerService aktoerService;
    @Mock
    private VeilederOppgaverService veilederOppgaverService;
    @Mock
    private BrukerprofilService brukerprofilService;
    @Mock
    private OrganisasjonService organisasjonService;
    @Mock
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;

    @InjectMocks
    private OppfoelgingsdialogRessurs oppfoelgingsdialogRessurs;

    @Test
    public void historikk_har_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);

        oppfoelgingsdialogRessurs.historikk(FNR);

        Mockito.verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void historikk_har_ikke_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        oppfoelgingsdialogRessurs.historikk(FNR);
    }

    @Test(expected = WebApplicationException.class)
    public void historikk_annen_tilgangsfeil() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(Statuses.from(500, "Tau i propellen"));

        oppfoelgingsdialogRessurs.historikk(FNR);
    }

    @Test
    public void hentDialoger_har_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);

        oppfoelgingsdialogRessurs.hentDialoger(FNR);

        Mockito.verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void hentDialoger_har_ikke_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        oppfoelgingsdialogRessurs.hentDialoger(FNR);
    }

    @Test(expected = WebApplicationException.class)
    public void hentDialoger_annen_tilgangsfeil() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(Statuses.from(500, "Sukker i bensinen"));

        oppfoelgingsdialogRessurs.hentDialoger(FNR);
    }

}
