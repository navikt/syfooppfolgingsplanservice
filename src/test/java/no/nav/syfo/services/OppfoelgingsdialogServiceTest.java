package no.nav.syfo.services;

import no.nav.brukerdialog.security.oidc.SystemUserTokenProvider;
import no.nav.syfo.domain.*;
import no.nav.syfo.domain.rs.RSOppfoelgingsplan;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.service.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Arrays;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OppfoelgingsdialogServiceTest {

    @Mock
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @Mock
    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;
    @Mock
    private GodkjentplanDAO godkjentplanDAO;
    @Mock
    private DokumentDAO dokumentDAO;
    @Mock
    private TiltakDAO tiltakDAO;
    @Mock
    private KommentarDAO kommentarDAO;
    @Mock
    private NaermesteLederService naermesteLederService;
    @Mock
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private AktoerService aktoerService;
    @Mock
    private BrukerprofilService brukerprofilService;
    @Mock
    private ServiceVarselService serviceVarselService;
    @Mock
    private TredjepartsvarselService tredjepartsvarselService;
    @Mock
    private VeilederOppgaverService veilederOppgaverService;
    @Mock
    private GodkjenningerDAO godkjenningerDAO;
    @Mock
    private Client client;
    @Mock
    private SystemUserTokenProvider systemUserTokenProvider;
    @InjectMocks
    private OppfoelgingsdialogService oppfoelgingsdialogService;

    @Before
    public void setup() {
    }


    @Test
    public void avbrytningAvEksisterendePlanFoererTilOpprettelseAvNyPlanMedDataFraGammelPlan() {
        Oppfoelgingsdialog oppfoelgingsdialog = new Oppfoelgingsdialog()
                .id(1L)
                .arbeidstaker(new Person()
                        .aktoerId("12345678901"))
                .arbeidsoppgaveListe(Arrays.asList(
                        new Arbeidsoppgave().id(1L)
                ))
                .tiltakListe(Arrays.asList(
                        new Tiltak()
                                .id(1L)
                                .kommentarer(Arrays.asList(
                                        new Kommentar()
                                ))
                ));
        when(oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(anyLong())).thenReturn(oppfoelgingsdialog);
        when(oppfoelingsdialogDAO.create(any())).thenReturn(oppfoelgingsdialog.id(2L));
        when(naermesteLederService.hentNaermesteLeder(anyString(), anyString())).thenReturn(of(new Naermesteleder()));
        when(aktoerService.hentAktoerIdForFnr(anyString())).thenReturn("1234567890123");
        when(tilgangskontrollService.aktoerTilhoererDialogen(anyString(), any())).thenReturn(true);
        when(tiltakDAO.create(any())).thenReturn(new Tiltak().id(1L));
        when(arbeidsoppgaveDAO.arbeidsoppgaverByOppfoelgingsdialogId(anyLong())).thenReturn(Arrays.asList(new Arbeidsoppgave().id(1L)));
        when(tiltakDAO.finnTiltakByOppfoelgingsdialogId(anyLong())).thenReturn(Arrays.asList(new Tiltak()
                .id(1L)
                .kommentarer(Arrays.asList(
                        new Kommentar()
                ))));
        oppfoelgingsdialogService.avbrytPlan(1L, "12345678901");

        verify(arbeidsoppgaveDAO).create(any());
        verify(tiltakDAO).create(any());
        verify(kommentarDAO).create(any());
    }

    @Test
    public void kopieringAvEksisterendePlanFoererTilOpprettelseAvNyPlanMedDataFraGammelPlan() {
        Oppfoelgingsdialog oppfoelgingsdialog = new Oppfoelgingsdialog()
                .id(1L)
                .arbeidstaker(new Person()
                        .aktoerId("12345678901"))
                .arbeidsoppgaveListe(Arrays.asList(
                        new Arbeidsoppgave().id(1L)
                ))
                .tiltakListe(Arrays.asList(
                        new Tiltak()
                                .id(1L)
                                .kommentarer(Arrays.asList(
                                        new Kommentar()
                                ))
                ));
        when(oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(anyLong())).thenReturn(oppfoelgingsdialog);
        when(oppfoelingsdialogDAO.create(any())).thenReturn(oppfoelgingsdialog.id(2L));
        when(naermesteLederService.hentNaermesteLeder(anyString(), anyString())).thenReturn(of(new Naermesteleder()));
        when(aktoerService.hentAktoerIdForFnr(anyString())).thenReturn("1234567890123");
        when(tilgangskontrollService.aktoerTilhoererDialogen(anyString(), any())).thenReturn(true);
        when(tiltakDAO.create(any())).thenReturn(new Tiltak().id(1L));
        when(arbeidsoppgaveDAO.arbeidsoppgaverByOppfoelgingsdialogId(anyLong())).thenReturn(Arrays.asList(new Arbeidsoppgave().id(1L)));
        when(tiltakDAO.finnTiltakByOppfoelgingsdialogId(anyLong())).thenReturn(Arrays.asList(new Tiltak()
                .id(1L)
                .kommentarer(Arrays.asList(
                        new Kommentar()
                ))));
        oppfoelgingsdialogService.kopierOppfoelgingsdialog(1L, "12345678901");

        verify(arbeidsoppgaveDAO).create(any());
        verify(tiltakDAO).create(any());
        verify(kommentarDAO).create(any());
    }

    @Test
    public void delMedFastlege() throws Exception {
        when(oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(anyLong())).thenReturn(new Oppfoelgingsdialog());
        when(aktoerService.hentAktoerIdForFnr(anyString())).thenReturn("aktoerId");
        when(tilgangskontrollService.aktoerTilhoererDialogen(eq("aktoerId"), any(Oppfoelgingsdialog.class))).thenReturn(true);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(anyLong())).thenReturn(of(new GodkjentPlan().dokumentUuid("dokumentUuid")));
        when(dokumentDAO.hent(anyString())).thenReturn(new byte[]{0, 1, 2});


        WebTarget webTarget = mock(WebTarget.class);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        Response response = mock(Response.class);
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.header(anyString(), any())).thenReturn(builder);
        when(builder.post(any(Entity.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(200);

        oppfoelgingsdialogService.delMedFastlege(1L, "fnr");

        ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
        verify(builder).post(captor.capture());

        assertThat(((RSOppfoelgingsplan) captor.getValue().getEntity()).getOppfolgingsplanPdf()).isEqualTo(new Byte[]{0, 1, 2});

        verify(godkjentplanDAO).delMedFastlege(1L);
    }

    @Test(expected = ForbiddenException.class)
    public void delMedFastlegeIkkeTilgang() throws Exception {
        when(oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(anyLong())).thenReturn(new Oppfoelgingsdialog());
        when(aktoerService.hentAktoerIdForFnr(anyString())).thenReturn("aktoerId");
        when(tilgangskontrollService.aktoerTilhoererDialogen(eq("aktoerId"), any(Oppfoelgingsdialog.class))).thenReturn(false);

        oppfoelgingsdialogService.delMedFastlege(1L, "fnr");
    }

    @Test(expected = RuntimeException.class)
    public void delMedFastlegeFinnerIkkeGodkjentPlan() throws Exception {
        when(oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(anyLong())).thenReturn(new Oppfoelgingsdialog());
        when(aktoerService.hentAktoerIdForFnr(anyString())).thenReturn("aktoerId");
        when(tilgangskontrollService.aktoerTilhoererDialogen(eq("aktoerId"), any(Oppfoelgingsdialog.class))).thenReturn(true);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(anyLong())).thenReturn(empty());

        oppfoelgingsdialogService.delMedFastlege(1L, "fnr");
    }

    @Test(expected = RuntimeException.class)
    public void delMedFastlegeFeilFraFastlegerest() throws Exception {
        when(oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(anyLong())).thenReturn(new Oppfoelgingsdialog());
        when(aktoerService.hentAktoerIdForFnr(anyString())).thenReturn("aktoerId");
        when(tilgangskontrollService.aktoerTilhoererDialogen(eq("aktoerId"), any(Oppfoelgingsdialog.class))).thenReturn(true);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(anyLong())).thenReturn(of(new GodkjentPlan().dokumentUuid("dokumentUuid")));
        when(dokumentDAO.hent(anyString())).thenReturn(new byte[]{0, 1, 2});


        WebTarget webTarget = mock(WebTarget.class);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        Response response = mock(Response.class);
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.header(anyString(), any())).thenReturn(builder);
        when(builder.post(any(Entity.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(400);

        oppfoelgingsdialogService.delMedFastlege(1L, "fnr");
    }
}
