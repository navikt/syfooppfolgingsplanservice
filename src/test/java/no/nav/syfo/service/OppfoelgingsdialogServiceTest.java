package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.LocalApplication;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.domain.*;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.oidc.OIDCIssuer;
import no.nav.syfo.repository.dao.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.Arrays;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static no.nav.syfo.service.FastlegeService.SEND_OPPFOLGINGSPLAN_PATH;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalApplication.class)
@DirtiesContext
public class OppfoelgingsdialogServiceTest {

    @MockBean
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @MockBean
    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;
    @MockBean
    private GodkjentplanDAO godkjentplanDAO;
    @MockBean
    private DokumentDAO dokumentDAO;
    @MockBean
    private TiltakDAO tiltakDAO;
    @MockBean
    private KommentarDAO kommentarDAO;
    @MockBean
    private NarmesteLederConsumer narmesteLederConsumer;
    @MockBean
    private TilgangskontrollService tilgangskontrollService;
    @MockBean
    private AktorregisterConsumer aktorregisterConsumer;
    @MockBean
    private BrukerprofilService brukerprofilService;
    @MockBean
    private ServiceVarselService serviceVarselService;
    @MockBean
    private TredjepartsvarselService tredjepartsvarselService;
    @MockBean
    private VeilederOppgaverService veilederOppgaverService;
    @MockBean
    private GodkjenningerDAO godkjenningerDAO;

    @Value("${fastlege.dialogmelding.api.v1.url}")
    private String fastlegerestUrl;

    @Inject
    public OIDCRequestContextHolder oidcRequestContextHolder;

    @Inject
    private RestTemplate restTemplate;

    private MockRestServiceServer mockRestServiceServer;

    @Inject
    private OppfoelgingsdialogService oppfoelgingsdialogService;

    @Before
    public void setUp() {
        this.mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);
    }

    @After
    public void tearDown() {
        loggUtAlle(oidcRequestContextHolder);
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
        when(naermesteLederService.hentNaermesteLeder(anyString(), anyString(), any())).thenReturn(of(new Naermesteleder()));
        when(aktorregisterConsumer.hentAktorIdForFnr(anyString())).thenReturn("1234567890123");
        when(narmesteLederConsumer.narmesteLeder(anyString(), anyString())).thenReturn(of(new Naermesteleder()));
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
        when(naermesteLederService.hentNaermesteLeder(anyString(), anyString(), any())).thenReturn(of(new Naermesteleder()));
        when(aktorregisterConsumer.hentAktorIdForFnr(anyString())).thenReturn("1234567890123");
        when(narmesteLederConsumer.narmesteLeder(anyString(), anyString())).thenReturn(of(new Naermesteleder()));
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
        mockSvarFraSendOppfolgingsplanTilFastlegerest(HttpStatus.OK);

        when(oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(anyLong())).thenReturn(new Oppfoelgingsdialog());
        when(aktorregisterConsumer.hentAktorIdForFnr(anyString())).thenReturn("aktoerId");
        when(tilgangskontrollService.aktoerTilhoererDialogen(eq("aktoerId"), any(Oppfoelgingsdialog.class))).thenReturn(true);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(anyLong())).thenReturn(of(new GodkjentPlan().dokumentUuid("dokumentUuid")));
        when(dokumentDAO.hent(anyString())).thenReturn(new byte[]{0, 1, 2});

        oppfoelgingsdialogService.delMedFastlege(1L, "fnr");

        verify(godkjentplanDAO).delMedFastlege(1L);

        mockRestServiceServer.verify();
    }

    @Test(expected = ForbiddenException.class)
    public void delMedFastlegeIkkeTilgang() throws Exception {
        when(oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(anyLong())).thenReturn(new Oppfoelgingsdialog());
        when(aktorregisterConsumer.hentAktorIdForFnr(anyString())).thenReturn("aktoerId");
        when(tilgangskontrollService.aktoerTilhoererDialogen(eq("aktoerId"), any(Oppfoelgingsdialog.class))).thenReturn(false);

        oppfoelgingsdialogService.delMedFastlege(1L, "fnr");
    }

    @Test(expected = RuntimeException.class)
    public void delMedFastlegeFinnerIkkeGodkjentPlan() throws Exception {
        when(oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(anyLong())).thenReturn(new Oppfoelgingsdialog());
        when(aktorregisterConsumer.hentAktorIdForFnr(anyString())).thenReturn("aktoerId");
        when(tilgangskontrollService.aktoerTilhoererDialogen(eq("aktoerId"), any(Oppfoelgingsdialog.class))).thenReturn(true);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(anyLong())).thenReturn(empty());

        oppfoelgingsdialogService.delMedFastlege(1L, "fnr");
    }

    @Test(expected = RuntimeException.class)
    public void delMedFastlegeFeilFraFastlegerest() throws Exception {
        mockSvarFraSendOppfolgingsplanTilFastlegerest(HttpStatus.INTERNAL_SERVER_ERROR);

        when(oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(anyLong())).thenReturn(new Oppfoelgingsdialog());
        when(aktorregisterConsumer.hentAktorIdForFnr(anyString())).thenReturn("aktoerId");
        when(tilgangskontrollService.aktoerTilhoererDialogen(eq("aktoerId"), any(Oppfoelgingsdialog.class))).thenReturn(true);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(anyLong())).thenReturn(of(new GodkjentPlan().dokumentUuid("dokumentUuid")));
        when(dokumentDAO.hent(anyString())).thenReturn(new byte[]{0, 1, 2});

        oppfoelgingsdialogService.delMedFastlege(1L, "fnr");

        mockRestServiceServer.verify();
    }

    public void mockSvarFraSendOppfolgingsplanTilFastlegerest(HttpStatus status) {
        String uriString = fromHttpUrl(fastlegerestUrl)
                .path(SEND_OPPFOLGINGSPLAN_PATH)
                .toUriString();

        String idToken = oidcRequestContextHolder.getOIDCValidationContext().getToken(OIDCIssuer.EKSTERN).getIdToken();

        mockRestServiceServer.expect(manyTimes(), requestTo(uriString))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(AUTHORIZATION, "Bearer " + idToken))
                .andRespond(withStatus(status));
    }
}
