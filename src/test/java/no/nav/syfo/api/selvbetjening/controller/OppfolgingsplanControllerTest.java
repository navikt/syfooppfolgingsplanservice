package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.api.selvbetjening.domain.*;
import no.nav.syfo.domain.Arbeidsoppgave;
import no.nav.syfo.domain.Tiltak;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;

import static no.nav.syfo.api.selvbetjening.mapper.RSArbeidsoppgaveMapper.rs2arbeidsoppgave;
import static no.nav.syfo.api.selvbetjening.mapper.RSTiltakMapper.rs2tiltak;
import static no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.KAN;
import static no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.TILRETTELEGGING;
import static no.nav.syfo.mock.MockSelvbetjeningRS.rsTiltakLagreEksisterende;
import static no.nav.syfo.mock.MockSelvbetjeningRS.rsTiltakLagreNytt;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static no.nav.syfo.util.MapUtil.map;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OppfolgingsplanControllerTest extends AbstractRessursTilgangTest {

    @Inject
    private OppfolgingsplanController oppfolgingsplanController;

    @MockBean
    Metrikk metrikk;
    @MockBean
    ArbeidsoppgaveService arbeidsoppgaveService;
    @MockBean
    GodkjenningService godkjenningService;
    @MockBean
    OppfoelgingsdialogService oppfoelgingsdialogService;
    @MockBean
    SamtykkeService samtykkeService;
    @MockBean
    TiltakService tiltakService;

    private static Long oppfolgingsplanId = 1L;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);
    }

    @Test
    public void avbryt_som_bruker() {
        oppfolgingsplanController.avbryt(oppfolgingsplanId);

        verify(oppfoelgingsdialogService).avbrytPlan(oppfolgingsplanId, ARBEIDSTAKER_FNR);
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void avbryt_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        oppfolgingsplanController.avbryt(oppfolgingsplanId);
    }

    @Test
    public void avvis_som_bruker() {
        oppfolgingsplanController.avvis(oppfolgingsplanId);

        verify(godkjenningService).avvisGodkjenning(oppfolgingsplanId, ARBEIDSTAKER_FNR);
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void avvis_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        oppfolgingsplanController.delMedNav(oppfolgingsplanId);
    }

    @Test
    public void delmedfastlege_som_bruker() {
        oppfolgingsplanController.delMedFastlege(oppfolgingsplanId);

        verify(oppfoelgingsdialogService).delMedFastlege(oppfolgingsplanId, ARBEIDSTAKER_FNR);
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void delmedfastlege_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        oppfolgingsplanController.delMedFastlege(oppfolgingsplanId);
    }

    @Test
    public void delmednav_som_bruker() {
        oppfolgingsplanController.delMedNav(oppfolgingsplanId);

        verify(oppfoelgingsdialogService).delMedNav(oppfolgingsplanId, ARBEIDSTAKER_FNR);
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void delmednav_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        oppfolgingsplanController.delMedNav(oppfolgingsplanId);
    }

    @Test
    public void kopier_som_bruker() {
        long nyPlanId = 1L;

        when(oppfoelgingsdialogService.kopierOppfoelgingsdialog(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(nyPlanId);

        long res = oppfolgingsplanController.kopier(oppfolgingsplanId);

        verify(metrikk).tellHendelse(anyString());

        assertEquals(res, nyPlanId);
    }

    @Test(expected = RuntimeException.class)
    public void kopier_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        oppfolgingsplanController.kopier(oppfolgingsplanId);
    }

    @Test
    public void lagrer_ny_arbeidsoppgave_som_bruker() {
        Long ressursId = 1L;
        RSArbeidsoppgave rsArbeidsoppgave = new RSArbeidsoppgave()
                .arbeidsoppgavenavn("Arbeidsoppgavenavn")
                .gjennomfoering(new RSGjennomfoering()
                        .kanGjennomfoeres(TILRETTELEGGING.name())
                        .medHjelp(true)
                        .medMerTid(true)
                        .paaAnnetSted(true)
                );

        Arbeidsoppgave arbeidsoppgave = map(rsArbeidsoppgave, rs2arbeidsoppgave);

        when(arbeidsoppgaveService.lagreArbeidsoppgave(oppfolgingsplanId, arbeidsoppgave, ARBEIDSTAKER_FNR)).thenReturn(ressursId);

        Long res = oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave);

        verify(arbeidsoppgaveService).lagreArbeidsoppgave(eq(oppfolgingsplanId), any(Arbeidsoppgave.class), eq(ARBEIDSTAKER_FNR));

        assertEquals(res, ressursId);
    }

    @Test
    public void lagrer_eksisterende_arbeidsoppgave_som_bruker() {
        Long arbeidsoppgaveId = 2L;
        RSArbeidsoppgave rsArbeidsoppgave = new RSArbeidsoppgave()
                .arbeidsoppgaveId(arbeidsoppgaveId)
                .arbeidsoppgavenavn("Arbeidsoppgavenavn")
                .gjennomfoering(new RSGjennomfoering()
                        .kanGjennomfoeres(KAN.name())
                        .kanBeskrivelse("Denne kan gjennomfoeres")
                );

        Arbeidsoppgave arbeidsoppgave = map(rsArbeidsoppgave, rs2arbeidsoppgave);

        when(arbeidsoppgaveService.lagreArbeidsoppgave(oppfolgingsplanId, arbeidsoppgave, ARBEIDSTAKER_FNR)).thenReturn(arbeidsoppgaveId);

        Long res = oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave);

        verify(arbeidsoppgaveService).lagreArbeidsoppgave(eq(oppfolgingsplanId), any(Arbeidsoppgave.class), eq(ARBEIDSTAKER_FNR));

        assertEquals(res, arbeidsoppgaveId);
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_lagre_arbeidsoppgave() {
        loggUtAlle(oidcRequestContextHolder);
        RSArbeidsoppgave rsArbeidsoppgave = new RSArbeidsoppgave();

        oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave);
    }

    @Test
    public void lagrer_tiltak_ny_som_bruker() {
        Long ressursId = 1L;
        RSTiltak rsTiltak = rsTiltakLagreNytt();

        Tiltak tiltak = map(rsTiltak, rs2tiltak);

        when(tiltakService.lagreTiltak(oppfolgingsplanId, tiltak, ARBEIDSTAKER_FNR)).thenReturn(ressursId);

        Long res = oppfolgingsplanController.lagreTiltak(oppfolgingsplanId, rsTiltak);

        verify(tiltakService).lagreTiltak(eq(oppfolgingsplanId), any(Tiltak.class), eq(ARBEIDSTAKER_FNR));

        assertEquals(res, ressursId);
    }

    @Test
    public void lagre_tiltak_eksisterende_som_bruker() {
        Long ressursId = 2L;
        RSTiltak rsTiltak = rsTiltakLagreEksisterende();

        Tiltak tiltak = map(rsTiltak, rs2tiltak);

        when(tiltakService.lagreTiltak(oppfolgingsplanId, tiltak, ARBEIDSTAKER_FNR)).thenReturn(ressursId);

        Long res = oppfolgingsplanController.lagreTiltak(oppfolgingsplanId, rsTiltak);

        verify(tiltakService).lagreTiltak(eq(oppfolgingsplanId), any(Tiltak.class), eq(ARBEIDSTAKER_FNR));

        assertEquals(res, ressursId);
    }

    @Test(expected = RuntimeException.class)
    public void lagre_tiltak_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);
        RSTiltak rsTiltak = new RSTiltak();

        oppfolgingsplanController.lagreTiltak(oppfolgingsplanId, rsTiltak);
    }

    @Test
    public void forespor_revidering_eksisterende_som_bruker() {
        oppfolgingsplanController.foresporRevidering(oppfolgingsplanId);

        verify(oppfoelgingsdialogService).foresporRevidering(oppfolgingsplanId, ARBEIDSTAKER_FNR);
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void forespor_revidering_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        oppfolgingsplanController.foresporRevidering(oppfolgingsplanId);
    }

    @Test
    public void nullstill_godkjenning_som_bruker() {
        oppfolgingsplanController.nullstillGodkjenning(oppfolgingsplanId);

        verify(oppfoelgingsdialogService).nullstillGodkjenning(oppfolgingsplanId, ARBEIDSTAKER_FNR);
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void nullstill_godkjenning_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        oppfolgingsplanController.foresporRevidering(oppfolgingsplanId);
    }

    @Test
    public void sett_som_bruker() {
        oppfolgingsplanController.sett(oppfolgingsplanId);

        verify(oppfoelgingsdialogService).oppdaterSistInnlogget(oppfolgingsplanId, ARBEIDSTAKER_FNR);
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void sett_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        oppfolgingsplanController.sett(oppfolgingsplanId);
    }

    @Test
    public void samtykk_som_bruker() {
        oppfolgingsplanController.samtykk(oppfolgingsplanId, true);

        verify(samtykkeService).giSamtykke(oppfolgingsplanId, ARBEIDSTAKER_FNR, true);
        verify(metrikk).tellHendelse(anyString());
    }

    @Test(expected = RuntimeException.class)
    public void samtykk_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder);

        oppfolgingsplanController.samtykk(oppfolgingsplanId, true);
    }
}