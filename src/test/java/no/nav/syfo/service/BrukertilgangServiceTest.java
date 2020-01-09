package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
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
    private AktorregisterConsumer aktorregisterConsumer;
    @Mock
    private NarmesteLederConsumer narmesteLederConsumer;
    @InjectMocks
    private BrukertilgangService brukertilgangService;

    private static final String INNLOGGET_FNR = "12345678901";
    private static final String INNLOGGET_AKTOERID = "1234567890123";
    private static final String SPOR_OM_FNR = "12345678902";
    private static final String SPOR_OM_AKTOERID = "1234567890122";

    @Before
    public void setup() {
        when(aktorregisterConsumer.hentAktorIdForFnr(INNLOGGET_FNR)).thenReturn(INNLOGGET_AKTOERID);
        when(aktorregisterConsumer.hentAktorIdForFnr(SPOR_OM_FNR)).thenReturn(SPOR_OM_AKTOERID);
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmSegSelv() {
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, INNLOGGET_FNR);
        assertThat(tilgang).isFalse();
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirFalseNaarManSporOmEnAnsatt() {
        when(narmesteLederConsumer.ansatte(INNLOGGET_AKTOERID)).thenReturn(Collections.singletonList(
                new Ansatt().aktoerId(SPOR_OM_AKTOERID)
        ));
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isFalse();
    }

    @Test
    public void sporOmNoenAndreEnnSegSelvGirTrueNaarManSporOmEnSomIkkeErSegSelvOgIkkeAnsatt() {
        when(narmesteLederConsumer.ansatte(INNLOGGET_AKTOERID)).thenReturn(Collections.emptyList());
        boolean tilgang = brukertilgangService.sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(INNLOGGET_FNR, SPOR_OM_FNR);
        assertThat(tilgang).isTrue();
    }
}
