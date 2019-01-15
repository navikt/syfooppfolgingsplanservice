package no.nav.syfo.api.intern.ressurs;

import no.nav.syfo.service.*;
import org.glassfish.jersey.message.internal.Statuses;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientBuilder.class)
public class EnhetRessursTilgangTest extends AbstractRessursTilgangTest {

    @Mock
    VeilederBehandlingService veilederBehandlingService;

    @InjectMocks
    private EnhetRessurs enhetRessurs;

    private static final String ENHET = "1234";

    @Test
    public void hentSykmeldte_har_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(200);

        enhetRessurs.hentSykmeldteMedUlesteOppfolgingsdialogerPaaEnhet(ENHET);

        verify(tilgangskontrollResponse).getStatus();
    }

    @Test(expected = ForbiddenException.class)
    public void hentSykmeldte_har_ikke_tilgang() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(403);

        enhetRessurs.hentSykmeldteMedUlesteOppfolgingsdialogerPaaEnhet(ENHET);
    }

    @Test(expected = WebApplicationException.class)
    public void hentDialoger_annen_tilgangsfeil() {
        when(tilgangskontrollResponse.getStatus()).thenReturn(500);
        when(tilgangskontrollResponse.getStatusInfo()).thenReturn(Statuses.from(500, "Sukker i bensinen"));

        enhetRessurs.hentSykmeldteMedUlesteOppfolgingsdialogerPaaEnhet(ENHET);
    }
}
