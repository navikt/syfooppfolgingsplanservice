package no.nav.syfo.service

import no.nav.syfo.api.selvbetjening.domain.RSGyldighetstidspunkt
import no.nav.syfo.dkif.DkifConsumer
import no.nav.syfo.domain.*
import no.nav.syfo.ereg.EregConsumer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.repository.dao.*
import no.nav.syfo.testhelper.any
import no.nav.syfo.testhelper.generateDigitalKontaktinfo
import no.nav.syfo.util.PropertyUtil
import no.nav.syfo.util.ToggleUtil
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@RunWith(SpringRunner::class)
class GodkjenningServiceTest {
    @Mock
    private lateinit var arbeidsforholdService: ArbeidsforholdService

    @Mock
    private lateinit var metrikk: Metrikk

    @Mock
    private lateinit var oppfolgingsplanDAO: OppfolgingsplanDAO

    @Mock
    private lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @Mock
    private lateinit var tilgangskontrollService: TilgangskontrollService

    @Mock
    private lateinit var pdlConsumer: PdlConsumer

    @Mock
    private lateinit var dkifConsumer: DkifConsumer

    @Mock
    private lateinit var godkjentplanDAO: GodkjentplanDAO

    @Mock
    private lateinit var dokumentDAO: DokumentDAO

    @Mock
    private lateinit var brukerprofilService: BrukerprofilService

    @Mock
    private lateinit var eregConsumer: EregConsumer

    @Mock
    private lateinit var serviceVarselService: ServiceVarselService

    @Mock
    private lateinit var tredjepartsvarselService: TredjepartsvarselService

    @Mock
    private lateinit var godkjenningerDAO: GodkjenningerDAO

    @InjectMocks
    private lateinit var godkjenningService: GodkjenningService

    private val oppfolgingsplan = Oppfolgingsplan()
        .id(1L)
        .arbeidsgiver(Person()
            .aktoerId("arbAktoerId")
        )
        .arbeidstaker(Person()
            .aktoerId("sykmeldtAktoerId")
        )
        .opprettet(LocalDateTime.now())
        .godkjenninger(Arrays.asList(Godkjenning()
            .godkjenningsTidspunkt(LocalDateTime.now())
            .godkjentAvAktoerId("arbAktoerId")
            .godkjent(true)
            .gyldighetstidspunkt(Gyldighetstidspunkt()
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .evalueres(LocalDate.now())
            ))
        )
        .opprettetAvAktoerId("sykmeldtAktoerId")
        .arbeidsoppgaveListe(Arrays.asList(
            Arbeidsoppgave()
                .opprettetAvAktoerId("arbAktoerId")
                .navn("navn")
                .opprettetDato(LocalDateTime.now())
        ))
        .tiltakListe(Arrays.asList(
            Tiltak()
                .beskrivelse("beskrivelse")
                .navn("navn")
        ))

    @Before
    fun setup() {
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Optional.of(Naermesteleder()
            .epost("epost")
            .mobil("mobil")
            .navn("navn")
        ))
        val digitalKontaktinfo = generateDigitalKontaktinfo()
        Mockito.`when`(dkifConsumer.kontaktinformasjon(ArgumentMatchers.anyString())).thenReturn(digitalKontaktinfo)
        Mockito.`when`(brukerprofilService.hentNavnByAktoerId(ArgumentMatchers.anyString())).thenReturn("navn")
        Mockito.`when`(pdlConsumer.fnr(ArgumentMatchers.anyString())).thenReturn("fnr")
        Mockito.`when`(eregConsumer.virksomhetsnavn(ArgumentMatchers.anyString())).thenReturn("Virksomhet")
        Mockito.`when`(arbeidsforholdService.arbeidstakersStillingerForOrgnummer(ArgumentMatchers.anyString(), any(), ArgumentMatchers.anyString())).thenReturn(emptyList())
        System.setProperty(PropertyUtil.ENVIRONMENT_NAME, ToggleUtil.ENVIRONMENT_MODE.dev.name)
    }

    @Test
    fun genererNyPlanKompatibelMedVersjon1Avplanen() {
        Mockito.`when`(godkjentplanDAO.create(ArgumentMatchers.any())).thenReturn(GodkjentPlan().id(1))
        godkjenningService.genererNyPlan(oppfolgingsplan, "sykmeldtAktoerId", false)
        Mockito.verify(godkjentplanDAO, Mockito.times(1)).create(ArgumentMatchers.any())
    }

    @Test
    fun genererNyPlanKompatibelMedVersjon2Avplanen() {
        Mockito.`when`(godkjentplanDAO.create(ArgumentMatchers.any())).thenReturn(GodkjentPlan().id(1))
        oppfolgingsplan.tiltakListe = Arrays.asList(Tiltak()
            .beskrivelse("beskrivelse")
            .navn("navn")
            .gjennomfoering("gjennomfoering")
            .opprettetAvAktoerId("opprettetAv")
            .status("status")
            .fom(LocalDate.now())
            .tom(LocalDate.now())
        )
        godkjenningService.genererNyPlan(oppfolgingsplan, "sykmeldtAktoerId", false)
        Mockito.verify(godkjentplanDAO, Mockito.times(1)).create(ArgumentMatchers.any())
    }

    @Test
    fun tvungenPlanKompatibelMedVersjon1Avplanen() {
        Mockito.`when`(godkjentplanDAO.create(ArgumentMatchers.any())).thenReturn(GodkjentPlan().id(1))
        godkjenningService.genererTvungenPlan(oppfolgingsplan, RSGyldighetstidspunkt()
            .tom(LocalDate.now())
            .fom(LocalDate.now())
            .evalueres(LocalDate.now()
            ), false
        )
        Mockito.verify(godkjentplanDAO, Mockito.times(1)).create(ArgumentMatchers.any())
    }

    @Test
    fun tvungenPlanKompatibelMedVersjon2Avplanen() {
        Mockito.`when`(godkjentplanDAO.create(ArgumentMatchers.any())).thenReturn(GodkjentPlan().id(1))
        oppfolgingsplan.tiltakListe = Arrays.asList(Tiltak()
            .beskrivelse("beskrivelse")
            .navn("navn")
            .gjennomfoering("gjennomfoering")
            .opprettetAvAktoerId("opprettetAv")
            .status("status")
            .fom(LocalDate.now())
            .tom(LocalDate.now())
        )
        godkjenningService.genererTvungenPlan(oppfolgingsplan, RSGyldighetstidspunkt()
            .tom(LocalDate.now())
            .fom(LocalDate.now())
            .evalueres(LocalDate.now()
            ), false
        )
        Mockito.verify(godkjentplanDAO, Mockito.times(1)).create(ArgumentMatchers.any())
    }
}
