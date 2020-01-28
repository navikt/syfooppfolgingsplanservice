package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.brukertilgang.BrukertilgangConsumer;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.testhelper.NarmesteLederGenerator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.inject.Inject;
import java.util.Optional;

import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.UserConstants.*;
import static no.nav.syfo.util.HeaderUtil.NAV_PERSONIDENT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class NarmesteLederControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    AktorregisterConsumer aktorregisterConsumer;
    @MockBean
    BrukertilgangConsumer brukertilgangConsumer;
    @MockBean
    NarmesteLederConsumer narmesteLederConsumer;

    @Inject
    private NarmesteLederController narmesteLederController;

    private NarmesteLederGenerator narmesteLederGenerator = new NarmesteLederGenerator();

    private Naermesteleder naermesteleder = narmesteLederGenerator.generateNarmesteLeder()
            .naermesteLederAktoerId(LEDER_AKTORID);
    private MultiValueMap<String, String> httpHeaders = getHttpHeaders();

    @Before
    public void setup() {
        when(aktorregisterConsumer.hentAktorIdForFnr(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID);
    }

    @Test
    public void getNarmesteLeder_ansatt_ok() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR);

        when(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true);
        when(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_AKTORID, VIRKSOMHETSNUMMER))
                .thenReturn(Optional.of(naermesteleder));

        ResponseEntity res = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER);
        Naermesteleder body = (Naermesteleder) res.getBody();

        assertEquals(200, res.getStatusCodeValue());
        assertEquals(naermesteleder, body);
    }

    @Test
    public void getNarmesteLeder_self_ok() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);

        when(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_AKTORID, VIRKSOMHETSNUMMER))
                .thenReturn(Optional.of(naermesteleder));

        ResponseEntity res = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER);
        Naermesteleder body = (Naermesteleder) res.getBody();

        assertEquals(200, res.getStatusCodeValue());
        assertEquals(naermesteleder, body);
    }

    @Test
    public void getNarmesteLeder_noContent() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR);

        when(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(true);
        when(narmesteLederConsumer.narmesteLeder(ARBEIDSTAKER_AKTORID, VIRKSOMHETSNUMMER)).thenReturn(Optional.empty());

        ResponseEntity res = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER);

        assertEquals(204, res.getStatusCodeValue());
    }

    @Test
    public void getNarmesteLeder_forbidden() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR);

        when(brukertilgangConsumer.hasAccessToAnsatt(ARBEIDSTAKER_FNR)).thenReturn(false);

        ResponseEntity res = narmesteLederController.getNarmesteLeder(httpHeaders, VIRKSOMHETSNUMMER);

        assertEquals(403, res.getStatusCodeValue());
    }

    private MultiValueMap<String, String> getHttpHeaders() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(NAV_PERSONIDENT, ARBEIDSTAKER_FNR);
        return headers;
    }
}
