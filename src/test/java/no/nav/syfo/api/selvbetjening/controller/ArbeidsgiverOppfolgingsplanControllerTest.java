package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.NaermesteLederService;
import no.nav.syfo.service.OppfoelgingsdialogService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSGIVER;
import static no.nav.syfo.mocks.AktoerMock.mockAktorId;
import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArbeidsgiverOppfolgingsplanControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    NaermesteLederService naermesteLederService;
    @MockBean
    OppfoelgingsdialogService oppfoelgingsdialogService;
    @MockBean
    Metrikk metrikk;

    @Inject
    private ArbeidsgiverOppfolgingsplanController arbeidsgiverOppfolgingsplanController;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR);
    }

    @Test
    public void hent_oppfolgingsplaner_som_arbeidsgiver() {
        arbeidsgiverOppfolgingsplanController.hentArbeidsgiversOppfolgingsplaner();

        verify(oppfoelgingsdialogService).hentAktoersOppfoelgingsdialoger(ARBEIDSGIVER, LEDER_FNR);
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void hent_oppfolgingsplaner_finner_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        arbeidsgiverOppfolgingsplanController.hentArbeidsgiversOppfolgingsplaner();
    }

    @Test
    public void opprett_oppfolgingsplan_som_arbeidsgiver() {
        Long ressursId = 1L;

        RSOpprettOppfoelgingsdialog rsOpprettOppfoelgingsdialog = new RSOpprettOppfoelgingsdialog()
                .sykmeldtFnr(ARBEIDSTAKER_FNR)
                .virksomhetsnummer(VIRKSOMHETSNUMMER);

        when(naermesteLederService.erAktorLederForAktor(mockAktorId(LEDER_FNR), mockAktorId(ARBEIDSTAKER_FNR), EKSTERN)).thenReturn(true);
        when(oppfoelgingsdialogService.opprettOppfoelgingsdialog(rsOpprettOppfoelgingsdialog, LEDER_FNR)).thenReturn(ressursId);

        Long res = arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(rsOpprettOppfoelgingsdialog);

        verify(metrikk).tellHendelse(anyString());

        assertEquals(res, ressursId);
    }

    @Test(expected = ForbiddenException.class)
    public void opprett_oppfolgingsplan_som_arbeidsgiver_ikke_leder_arbeidstaker() {
        RSOpprettOppfoelgingsdialog rsOpprettOppfoelgingsdialog = new RSOpprettOppfoelgingsdialog()
                .sykmeldtFnr(LEDER_FNR)
                .virksomhetsnummer(VIRKSOMHETSNUMMER);

        when(naermesteLederService.erAktorLederForAktor(LEDER_AKTORID, ARBEIDSTAKER_AKTORID, EKSTERN)).thenReturn(false);

        arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(rsOpprettOppfoelgingsdialog);
    }

    @Test(expected = RuntimeException.class)
    public void opprett_oppfolgingsplan_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        RSOpprettOppfoelgingsdialog rsOpprettOppfoelgingsdialog = new RSOpprettOppfoelgingsdialog();

        arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(rsOpprettOppfoelgingsdialog);
    }
}
