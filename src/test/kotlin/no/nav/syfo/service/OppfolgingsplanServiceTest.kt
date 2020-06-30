package no.nav.syfo.service

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.domain.*
import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.repository.dao.*
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.any
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class OppfolgingsplanServiceTest {
    @MockBean
    private lateinit var oppfolgingsplanDAO: OppfolgingsplanDAO

    @MockBean
    private lateinit var arbeidsoppgaveDAO: ArbeidsoppgaveDAO

    @MockBean
    private lateinit var godkjentplanDAO: GodkjentplanDAO

    @MockBean
    private lateinit var dokumentDAO: DokumentDAO

    @MockBean
    private lateinit var tiltakDAO: TiltakDAO

    @MockBean
    private lateinit var kommentarDAO: KommentarDAO

    @MockBean
    private lateinit var narmesteLederConsumer: NarmesteLederConsumer

    @MockBean
    private lateinit var tilgangskontrollService: TilgangskontrollService

    @MockBean
    private lateinit var aktorregisterConsumer: AktorregisterConsumer

    @MockBean
    private lateinit var serviceVarselService: ServiceVarselService

    @MockBean
    private lateinit var tredjepartsvarselService: TredjepartsvarselService

    @MockBean
    private lateinit var godkjenningerDAO: GodkjenningerDAO

    @Value("\${fastlege.dialogmelding.api.v1.url}")
    private lateinit var fastlegerestUrl: String

    @Inject
    lateinit var oidcRequestContextHolder: OIDCRequestContextHolder

    @Inject
    private lateinit var restTemplate: RestTemplate
    private lateinit var mockRestServiceServer: MockRestServiceServer

    @Inject
    private lateinit var oppfolgingsplanService: OppfolgingsplanService

    @Before
    fun setUp() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR)
    }

    @After
    fun tearDown() {
        loggUtAlle(oidcRequestContextHolder)
    }

    @Test
    fun avbrytningAvEksisterendePlanFoererTilOpprettelseAvNyPlanMedDataFraGammelPlan() {
        val oppfolgingsplan = Oppfolgingsplan()
            .id(1L)
            .arbeidstaker(Person()
                .aktoerId("12345678901"))
            .arbeidsoppgaveListe(listOf(
                Arbeidsoppgave().id(1L)
            ))
            .tiltakListe(listOf(
                Tiltak()
                    .id(1L)
                    .kommentarer(listOf(
                        Kommentar()
                    ))
            ))
        Mockito.`when`(oppfolgingsplanDAO.finnOppfolgingsplanMedId(ArgumentMatchers.anyLong())).thenReturn(oppfolgingsplan)
        Mockito.`when`(oppfolgingsplanDAO.create(ArgumentMatchers.any())).thenReturn(oppfolgingsplan.id(2L))
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ArgumentMatchers.anyString())).thenReturn("1234567890123")
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Optional.of(Naermesteleder()))
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ArgumentMatchers.anyString())).thenReturn("1234567890123")
        Mockito.`when`(tilgangskontrollService.aktorTilhorerOppfolgingsplan(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(true)
        Mockito.`when`(tiltakDAO.create(any())).thenReturn(Tiltak().id(1L))
        Mockito.`when`(arbeidsoppgaveDAO.arbeidsoppgaverByOppfoelgingsdialogId(ArgumentMatchers.anyLong())).thenReturn(listOf(Arbeidsoppgave().id(1L)))
        Mockito.`when`(tiltakDAO.finnTiltakByOppfoelgingsdialogId(ArgumentMatchers.anyLong())).thenReturn(listOf(Tiltak()
            .id(1L)
            .kommentarer(listOf(
                Kommentar()
            ))))
        oppfolgingsplanService.avbrytPlan(1L, "12345678901")
        Mockito.verify(arbeidsoppgaveDAO).create(ArgumentMatchers.any())
        Mockito.verify(tiltakDAO).create(any())
        Mockito.verify(kommentarDAO).create(any())
    }

    @Test
    fun kopieringAvEksisterendePlanFoererTilOpprettelseAvNyPlanMedDataFraGammelPlan() {
        val oppfolgingsplan = Oppfolgingsplan()
            .id(1L)
            .arbeidstaker(Person()
                .aktoerId("12345678901"))
            .arbeidsoppgaveListe(listOf(
                Arbeidsoppgave().id(1L)
            ))
            .tiltakListe(listOf(
                Tiltak()
                    .id(1L)
                    .kommentarer(listOf(
                        Kommentar()
                    ))
            ))
        Mockito.`when`(oppfolgingsplanDAO.finnOppfolgingsplanMedId(ArgumentMatchers.anyLong())).thenReturn(oppfolgingsplan)
        Mockito.`when`(oppfolgingsplanDAO.create(ArgumentMatchers.any())).thenReturn(oppfolgingsplan.id(2L))
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ArgumentMatchers.anyString())).thenReturn("1234567890123")
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Optional.of(Naermesteleder()))
        Mockito.`when`(tilgangskontrollService.aktorTilhorerOppfolgingsplan(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(true)
        Mockito.`when`(tiltakDAO.create(any())).thenReturn(Tiltak().id(1L))
        Mockito.`when`(arbeidsoppgaveDAO.arbeidsoppgaverByOppfoelgingsdialogId(ArgumentMatchers.anyLong())).thenReturn(listOf(Arbeidsoppgave().id(1L)))
        Mockito.`when`(tiltakDAO.finnTiltakByOppfoelgingsdialogId(ArgumentMatchers.anyLong())).thenReturn(listOf(Tiltak()
            .id(1L)
            .kommentarer(listOf(
                Kommentar()
            ))))
        oppfolgingsplanService.kopierOppfoelgingsdialog(1L, "12345678901")
        Mockito.verify(arbeidsoppgaveDAO).create(ArgumentMatchers.any())
        Mockito.verify(tiltakDAO).create(any())
        Mockito.verify(kommentarDAO).create(any())
    }

    @Test
    @Throws(Exception::class)
    fun delMedFastlege() {
        mockSvarFraSendOppfolgingsplanTilFastlegerest(HttpStatus.OK)
        Mockito.`when`(oppfolgingsplanDAO.finnOppfolgingsplanMedId(ArgumentMatchers.anyLong())).thenReturn(Oppfolgingsplan())
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ArgumentMatchers.anyString())).thenReturn("aktoerId")
        Mockito.`when`(tilgangskontrollService.aktorTilhorerOppfolgingsplan(ArgumentMatchers.eq("aktoerId"), ArgumentMatchers.any(Oppfolgingsplan::class.java))).thenReturn(true)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(ArgumentMatchers.anyLong())).thenReturn(Optional.of(GodkjentPlan().dokumentUuid("dokumentUuid")))
        Mockito.`when`(dokumentDAO.hent(ArgumentMatchers.anyString())).thenReturn(byteArrayOf(0, 1, 2))
        oppfolgingsplanService.delMedFastlege(1L, "fnr")
        Mockito.verify(godkjentplanDAO).delMedFastlege(1L)
        mockRestServiceServer.verify()
    }

    @Test(expected = ForbiddenException::class)
    @Throws(Exception::class)
    fun delMedFastlegeIkkeTilgang() {
        Mockito.`when`(oppfolgingsplanDAO.finnOppfolgingsplanMedId(ArgumentMatchers.anyLong())).thenReturn(Oppfolgingsplan())
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ArgumentMatchers.anyString())).thenReturn("aktoerId")
        Mockito.`when`(tilgangskontrollService.aktorTilhorerOppfolgingsplan(ArgumentMatchers.eq("aktoerId"), ArgumentMatchers.any(Oppfolgingsplan::class.java))).thenReturn(false)
        oppfolgingsplanService.delMedFastlege(1L, "fnr")
    }

    @Test(expected = RuntimeException::class)
    @Throws(Exception::class)
    fun delMedFastlegeFinnerIkkeGodkjentPlan() {
        Mockito.`when`(oppfolgingsplanDAO.finnOppfolgingsplanMedId(ArgumentMatchers.anyLong())).thenReturn(Oppfolgingsplan())
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ArgumentMatchers.anyString())).thenReturn("aktoerId")
        Mockito.`when`(tilgangskontrollService.aktorTilhorerOppfolgingsplan(ArgumentMatchers.eq("aktoerId"), ArgumentMatchers.any(Oppfolgingsplan::class.java))).thenReturn(true)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(ArgumentMatchers.anyLong())).thenReturn(Optional.empty())
        oppfolgingsplanService.delMedFastlege(1L, "fnr")
    }

    @Test(expected = RuntimeException::class)
    @Throws(Exception::class)
    fun delMedFastlegeFeilFraFastlegerest() {
        mockSvarFraSendOppfolgingsplanTilFastlegerest(HttpStatus.INTERNAL_SERVER_ERROR)
        Mockito.`when`(oppfolgingsplanDAO.finnOppfolgingsplanMedId(ArgumentMatchers.anyLong())).thenReturn(Oppfolgingsplan())
        Mockito.`when`(aktorregisterConsumer.hentAktorIdForFnr(ArgumentMatchers.anyString())).thenReturn("aktoerId")
        Mockito.`when`(tilgangskontrollService.aktorTilhorerOppfolgingsplan(ArgumentMatchers.eq("aktoerId"), ArgumentMatchers.any(Oppfolgingsplan::class.java))).thenReturn(true)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(ArgumentMatchers.anyLong())).thenReturn(Optional.of(GodkjentPlan().dokumentUuid("dokumentUuid")))
        Mockito.`when`(dokumentDAO.hent(ArgumentMatchers.anyString())).thenReturn(byteArrayOf(0, 1, 2))
        oppfolgingsplanService.delMedFastlege(1L, "fnr")
        mockRestServiceServer.verify()
    }

    fun mockSvarFraSendOppfolgingsplanTilFastlegerest(status: HttpStatus) {
        val uriString = UriComponentsBuilder.fromHttpUrl(fastlegerestUrl)
            .path(FastlegeService.SEND_OPPFOLGINGSPLAN_PATH)
            .toUriString()
        val idToken = oidcRequestContextHolder.oidcValidationContext.getToken(OIDCIssuer.EKSTERN).idToken
        mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(uriString))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer $idToken"))
            .andRespond(MockRestResponseCreators.withStatus(status))
    }
}
