package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.service.OppfolgingsplanService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSGIVER;
import static no.nav.syfo.mocks.AktoerMock.mockAktorId;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArbeidsgiverOppfolgingsplanControllerTest extends AbstractRessursTilgangTest {

    @MockBean
    AktorregisterConsumer aktorregisterConsumer;
    @MockBean
    NarmesteLederConsumer narmesteLederConsumer;
    @MockBean
    OppfolgingsplanService oppfolgingsplanService;
    @MockBean
    Metrikk metrikk;

    @Inject
    private ArbeidsgiverOppfolgingsplanController arbeidsgiverOppfolgingsplanController;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR);
        mockAktorregisterConsumer();
    }

    @Test
    public void hent_oppfolgingsplaner_som_arbeidsgiver() {
        arbeidsgiverOppfolgingsplanController.hentArbeidsgiversOppfolgingsplaner();

        verify(oppfolgingsplanService).hentAktorsOppfolgingsplaner(ARBEIDSGIVER, LEDER_FNR);
        verify(metrikk).tellHendelse("hent_oppfolgingsplan_ag");
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

        when(narmesteLederConsumer.erAktorLederForAktor(mockAktorId(LEDER_FNR), mockAktorId(ARBEIDSTAKER_FNR))).thenReturn(true);
        when(oppfolgingsplanService.opprettOppfolgingsplan(rsOpprettOppfoelgingsdialog, LEDER_FNR)).thenReturn(ressursId);

        Long res = arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(rsOpprettOppfoelgingsdialog);

        verify(metrikk).tellHendelse("opprett_oppfolgingsplan_ag");

        assertEquals(res, ressursId);
    }

    @Test(expected = ForbiddenException.class)
    public void opprett_oppfolgingsplan_som_arbeidsgiver_ikke_leder_arbeidstaker() {
        RSOpprettOppfoelgingsdialog rsOpprettOppfoelgingsdialog = new RSOpprettOppfoelgingsdialog()
                .sykmeldtFnr(LEDER_FNR)
                .virksomhetsnummer(VIRKSOMHETSNUMMER);

        when(narmesteLederConsumer.erAktorLederForAktor(LEDER_AKTORID, ARBEIDSTAKER_AKTORID)).thenReturn(false);

        arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(rsOpprettOppfoelgingsdialog);
    }

    @Test(expected = RuntimeException.class)
    public void opprett_oppfolgingsplan_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        RSOpprettOppfoelgingsdialog rsOpprettOppfoelgingsdialog = new RSOpprettOppfoelgingsdialog();

        arbeidsgiverOppfolgingsplanController.opprettOppfolgingsplanSomArbeidsgiver(rsOpprettOppfoelgingsdialog);
    }

    private void mockAktorregisterConsumer() {
        when(aktorregisterConsumer.hentFnrForAktor(ARBEIDSTAKER_AKTORID)).thenReturn(ARBEIDSTAKER_FNR);
        when(aktorregisterConsumer.hentAktorIdForFnr(ARBEIDSTAKER_FNR)).thenReturn(ARBEIDSTAKER_AKTORID);

        when(aktorregisterConsumer.hentFnrForAktor(LEDER_AKTORID)).thenReturn(LEDER_FNR);
        when(aktorregisterConsumer.hentAktorIdForFnr(LEDER_FNR)).thenReturn(LEDER_AKTORID);
    }
}
