package no.nav.syfo.service;

import no.nav.syfo.model.Ansatt;
import no.nav.syfo.oidc.OIDCIssuer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrukertilgangServiceTest {

    @Mock
    private AktoerService aktoerService;
    @Mock
    private NaermesteLederService naermesteLederService;
    @InjectMocks
    private BrukertilgangService brukertilgangService;

    private static final String INNLOGGET_FNR = "12345678901";
    private static final String INNLOGGET_AKTOERID = "1234567890123";
    private static final String SPOR_OM_FNR = "12345678902";
    private static final String SPOR_OM_AKTOERID = "1234567890122";

    @Before
    public void setup() {
        when(aktoerService.hentAktoerIdForFnr(INNLOGGET_FNR)).thenReturn(INNLOGGET_AKTOERID);
        when(aktoerService.hentAktoerIdForFnr(SPOR_OM_FNR)).thenReturn(SPOR_OM_AKTOERID);
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmSegSelv() {
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, INNLOGGET_FNR);
        assertThat(tilgang).isFalse();
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmEnAnsatt() {
        when(naermesteLederService.hentAnsatte(INNLOGGET_AKTOERID, OIDCIssuer.EKSTERN)).thenReturn(Collections.singletonList(
                new Ansatt().aktoerId(SPOR_OM_AKTOERID)
        ));
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isFalse();
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirTrueNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        when(naermesteLederService.hentAnsatte(INNLOGGET_AKTOERID, OIDCIssuer.EKSTERN)).thenReturn(Collections.emptyList());
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isTrue();
    }
}