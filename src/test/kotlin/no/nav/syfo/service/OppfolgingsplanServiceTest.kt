package no.nav.syfo.service

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.LocalApplication
import no.nav.syfo.dialogmelding.DialogmeldingService
import no.nav.syfo.domain.*
import no.nav.syfo.model.Naermesteleder
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.repository.dao.*
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.any
import no.nav.syfo.tokenx.tokendings.TokenDingsConsumer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
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
    private lateinit var pdlConsumer: PdlConsumer

    @MockBean
    private lateinit var serviceVarselService: ServiceVarselService

    @MockBean
    private lateinit var tredjepartsvarselService: TredjepartsvarselService

    @MockBean
    private lateinit var godkjenningerDAO: GodkjenningerDAO

    @MockBean
    lateinit var tokenDingsConsumer: TokenDingsConsumer

    @Value("\${isdialogmelding.url}")
    private lateinit var isdialogmeldingUrl: String

    @Inject
    lateinit var contextHolder: TokenValidationContextHolder

    @Inject
    private lateinit var restTemplate: RestTemplate
    private lateinit var mockRestServiceServer: MockRestServiceServer

    @Inject
    private lateinit var oppfolgingsplanService: OppfolgingsplanService

    @Before
    fun setUp() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        loggInnBruker(contextHolder, ARBEIDSTAKER_FNR)
    }

    @After
    fun tearDown() {
        loggUtAlle(contextHolder)
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
        Mockito.`when`(pdlConsumer.aktorid(ArgumentMatchers.anyString())).thenReturn("1234567890123")
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Optional.of(Naermesteleder()))
        Mockito.`when`(pdlConsumer.aktorid(ArgumentMatchers.anyString())).thenReturn("1234567890123")
        Mockito.`when`(tilgangskontrollService.brukerTilhorerOppfolgingsplan(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(true)
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
        Mockito.`when`(pdlConsumer.aktorid(ArgumentMatchers.anyString())).thenReturn("1234567890123")
        Mockito.`when`(narmesteLederConsumer.narmesteLeder(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Optional.of(Naermesteleder()))
        Mockito.`when`(tilgangskontrollService.brukerTilhorerOppfolgingsplan(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(true)
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
        val aktoerId = "aktoerId"
        val fnr = "fnr"
        val oppfolgingsplan = Oppfolgingsplan()
        oppfolgingsplan.arbeidstaker.aktoerId(aktoerId)
        mockSvarFraSendOppfolgingsplanTilIsDialogmelding(HttpStatus.OK)
        Mockito.`when`(oppfolgingsplanDAO.finnOppfolgingsplanMedId(ArgumentMatchers.anyLong())).thenReturn(oppfolgingsplan)
        Mockito.`when`(pdlConsumer.fnr(aktoerId)).thenReturn(fnr)
        Mockito.`when`(tilgangskontrollService.brukerTilhorerOppfolgingsplan(ArgumentMatchers.eq(fnr), ArgumentMatchers.any(Oppfolgingsplan::class.java))).thenReturn(true)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(ArgumentMatchers.anyLong())).thenReturn(Optional.of(GodkjentPlan().dokumentUuid("dokumentUuid")))
        Mockito.`when`(dokumentDAO.hent(ArgumentMatchers.anyString())).thenReturn(byteArrayOf(0, 1, 2))

        oppfolgingsplanService.delMedFastlege(1L, fnr)

        Mockito.verify(godkjentplanDAO).delMedFastlege(1L)
        mockRestServiceServer.verify()
    }

    @Test(expected = ForbiddenException::class)
    @Throws(Exception::class)
    fun delMedFastlegeIkkeTilgang() {
        Mockito.`when`(oppfolgingsplanDAO.finnOppfolgingsplanMedId(ArgumentMatchers.anyLong())).thenReturn(Oppfolgingsplan())
        Mockito.`when`(pdlConsumer.aktorid(ArgumentMatchers.anyString())).thenReturn("aktoerId")
        Mockito.`when`(tilgangskontrollService.brukerTilhorerOppfolgingsplan(ArgumentMatchers.eq("fnr"), ArgumentMatchers.any(Oppfolgingsplan::class.java))).thenReturn(false)
        oppfolgingsplanService.delMedFastlege(1L, "fnr")
    }

    @Test(expected = RuntimeException::class)
    @Throws(Exception::class)
    fun delMedFastlegeFinnerIkkeGodkjentPlan() {
        Mockito.`when`(oppfolgingsplanDAO.finnOppfolgingsplanMedId(ArgumentMatchers.anyLong())).thenReturn(Oppfolgingsplan())
        Mockito.`when`(pdlConsumer.aktorid(ArgumentMatchers.anyString())).thenReturn("aktoerId")
        Mockito.`when`(tilgangskontrollService.brukerTilhorerOppfolgingsplan(ArgumentMatchers.eq("fnr"), ArgumentMatchers.any(Oppfolgingsplan::class.java))).thenReturn(true)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(ArgumentMatchers.anyLong())).thenReturn(Optional.empty())
        oppfolgingsplanService.delMedFastlege(1L, "fnr")
    }

    @Test(expected = RuntimeException::class)
    @Throws(Exception::class)
    fun delMedFastlegeFeilFraFastlegerest() {
        mockSvarFraSendOppfolgingsplanTilIsDialogmelding(HttpStatus.INTERNAL_SERVER_ERROR)
        Mockito.`when`(oppfolgingsplanDAO.finnOppfolgingsplanMedId(ArgumentMatchers.anyLong())).thenReturn(Oppfolgingsplan())
        Mockito.`when`(pdlConsumer.aktorid(ArgumentMatchers.anyString())).thenReturn("aktoerId")
        Mockito.`when`(tilgangskontrollService.brukerTilhorerOppfolgingsplan(ArgumentMatchers.eq("fnr"), ArgumentMatchers.any(Oppfolgingsplan::class.java))).thenReturn(true)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(ArgumentMatchers.anyLong())).thenReturn(Optional.of(GodkjentPlan().dokumentUuid("dokumentUuid")))
        Mockito.`when`(dokumentDAO.hent(ArgumentMatchers.anyString())).thenReturn(byteArrayOf(0, 1, 2))
        oppfolgingsplanService.delMedFastlege(1L, "fnr")
        mockRestServiceServer.verify()
    }

    fun mockSvarFraSendOppfolgingsplanTilIsDialogmelding(status: HttpStatus) {
        val token = "token"
        Mockito.`when`(tokenDingsConsumer.exchangeToken(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(token)
        val uriString = UriComponentsBuilder.fromHttpUrl(isdialogmeldingUrl)
            .path(DialogmeldingService.SEND_OPPFOLGINGSPLAN_PATH)
            .toUriString()
        mockRestServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(uriString))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
            .andRespond(MockRestResponseCreators.withStatus(status))
    }
}
