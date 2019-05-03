package no.nav.syfo.service;

import no.nav.syfo.api.selvbetjening.domain.RSGyldighetstidspunkt;
import no.nav.syfo.domain.*;
import no.nav.syfo.domain.sykmelding.Periode;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Kontaktinfo;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.util.ToggleUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static no.nav.syfo.util.PropertyUtil.ENVIRONMENT_NAME;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class GodkjenningServiceTest {

    @Mock
    private Metrikk metrikk;
    @Mock
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @Mock
    private NaermesteLederService naermesteLederService;
    @Mock
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private AktoerService aktoerService;
    @Mock
    private DkifService dkifService;
    @Mock
    private GodkjentplanDAO godkjentplanDAO;
    @Mock
    private DokumentDAO dokumentDAO;
    @Mock
    private BrukerprofilService brukerprofilService;
    @Mock
    private OrganisasjonService organisasjonService;
    @Mock
    private ArbeidsforholdService arbeidsforholdService;
    @Mock
    private ServiceVarselService serviceVarselService;
    @Mock
    private TredjepartsvarselService tredjepartsvarselService;
    @Mock
    private SykeforloepService sykeforloepService;
    @Mock
    private GodkjenningerDAO godkjenningerDAO;
    @InjectMocks
    private GodkjenningService godkjenningService;


    private Oppfoelgingsdialog oppfoelgingsdialog = new Oppfoelgingsdialog()
            .id(1L)
            .arbeidsgiver(new Person()
                    .aktoerId("arbAktoerId")
            )
            .arbeidstaker(new Person()
                    .aktoerId("sykmeldtAktoerId")
            )
            .opprettet(LocalDateTime.now())
            .godkjenninger(asList(new Godkjenning()
                    .godkjenningsTidspunkt(LocalDateTime.now())
                    .godkjentAvAktoerId("arbAktoerId")
                    .godkjent(true)
                    .gyldighetstidspunkt(new Gyldighetstidspunkt()
                            .fom(now())
                            .tom(now())
                            .evalueres(now())
                    ))
            )
            .opprettetAvAktoerId("sykmeldtAktoerId")
            .arbeidsoppgaveListe((asList(
                    new Arbeidsoppgave()
                            .opprettetAvAktoerId("arbAktoerId")
                            .navn("navn")
                            .opprettetDato(LocalDateTime.now())
            )))
            .tiltakListe(asList(
                    new Tiltak()
                            .beskrivelse("beskrivelse")
                            .navn("navn")
            ));

    @Before
    public void setup() {
        when(naermesteLederService.hentNaermesteLeder(any(), any(), any())).thenReturn(Optional.of(new Naermesteleder()
                .epost("epost")
                .mobil("mobil")
                .navn("navn")
        ));
        when(dkifService.hentKontaktinfoAktoerId(anyString())).thenReturn(new Kontaktinfo().epost("epost").tlf("tlf"));
        when(brukerprofilService.hentNavnByAktoerId(anyString())).thenReturn("navn");
        when(aktoerService.hentFnrForAktoer(anyString())).thenReturn("fnr");
        when(organisasjonService.finnVirksomhetsnavn(anyString())).thenReturn("Virksomhet");
        when(arbeidsforholdService.hentArbeidsforholdMedAktoerId(anyString(), any(), anyString())).thenReturn(Collections.emptyList());
        when(sykeforloepService.hentSykeforlopperiode(anyString(), anyString())).thenReturn(
                asList(new Periode()
                        .withFom(now().minusDays(2))
                        .withTom(now().plusDays(2))
                        .withGrad(100)
                        .withAvventende(true)
                        .withReisetilskudd(false)
                        .withBehandlingsdager(true))
        );
        System.setProperty(ENVIRONMENT_NAME, ToggleUtil.ENVIRONMENT_MODE.dev.name());
    }

    @Test
    public void genererNyPlanKompatibelMedVersjon1Avplanen() {
        when(godkjentplanDAO.create(any())).thenReturn(new GodkjentPlan().id(1));
        godkjenningService.genererNyPlan(oppfoelgingsdialog, "sykmeldtAktoerId");
        verify(godkjentplanDAO, times(1)).create(any());
    }

    @Test
    public void genererNyPlanKompatibelMedVersjon2Avplanen() {
        when(godkjentplanDAO.create(any())).thenReturn(new GodkjentPlan().id(1));
        oppfoelgingsdialog.tiltakListe = asList(new Tiltak()
                .beskrivelse("beskrivelse")
                .navn("navn")
                .gjennomfoering("gjennomfoering")
                .opprettetAvAktoerId("opprettetAv")
                .status("status")
                .fom(now())
                .tom(now())
        );
        godkjenningService.genererNyPlan(oppfoelgingsdialog, "sykmeldtAktoerId");
        verify(godkjentplanDAO, times(1)).create(any());
    }

    @Test
    public void tvungenPlanKompatibelMedVersjon1Avplanen() {
        when(godkjentplanDAO.create(any())).thenReturn(new GodkjentPlan().id(1));
        godkjenningService.genererTvungenPlan(oppfoelgingsdialog, new RSGyldighetstidspunkt()
                .tom(now())
                .fom(now())
                .evalueres(now()));
        verify(godkjentplanDAO, times(1)).create(any());
    }

    @Test
    public void tvungenPlanKompatibelMedVersjon2Avplanen() {
        when(godkjentplanDAO.create(any())).thenReturn(new GodkjentPlan().id(1));
        oppfoelgingsdialog.tiltakListe = asList(new Tiltak()
                .beskrivelse("beskrivelse")
                .navn("navn")
                .gjennomfoering("gjennomfoering")
                .opprettetAvAktoerId("opprettetAv")
                .status("status")
                .fom(now())
                .tom(now())
        );
        godkjenningService.genererTvungenPlan(oppfoelgingsdialog, new RSGyldighetstidspunkt()
                .tom(now())
                .fom(now())
                .evalueres(now())
        );
        verify(godkjentplanDAO, times(1)).create(any());
    }
}
