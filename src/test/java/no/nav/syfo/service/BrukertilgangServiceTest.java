package no.nav.syfo.service;

import no.nav.syfo.brukertilgang.BrukertilgangConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrukertilgangServiceTest {

    @Mock
    private BrukertilgangConsumer brukertilgangConsumer;
    @InjectMocks
    private BrukertilgangService brukertilgangService;

    private static final String INNLOGGET_FNR = "12345678901";
    private static final String INNLOGGET_AKTOERID = "1234567890123";
    private static final String SPOR_OM_FNR = "12345678902";
    private static final String SPOR_OM_AKTOERID = "1234567890122";

    @Before
    public void setup() {
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmSegSelv() {
        boolean tilgang = brukertilgangService.tilgangTilOppslattIdent(INNLOGGET_FNR, INNLOGGET_FNR);
        assertThat(tilgang).isTrue();
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmEnAnsatt() {
        when(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(true);
        boolean tilgang = brukertilgangService.tilgangTilOppslattIdent(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isTrue();
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirTrueNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        when(brukertilgangConsumer.hasAccessToAnsatt(SPOR_OM_FNR)).thenReturn(false);
        boolean tilgang = brukertilgangService.tilgangTilOppslattIdent(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isFalse();
    }
}
