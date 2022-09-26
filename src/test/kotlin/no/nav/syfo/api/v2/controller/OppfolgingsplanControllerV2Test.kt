package no.nav.syfo.api.v2.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSGIVER
import no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSTAKER
import no.nav.syfo.api.selvbetjening.domain.RSArbeidsoppgave
import no.nav.syfo.api.selvbetjening.domain.RSGjennomfoering
import no.nav.syfo.api.selvbetjening.domain.RSGyldighetstidspunkt
import no.nav.syfo.api.selvbetjening.domain.RSTiltak
import no.nav.syfo.api.selvbetjening.mapper.RSArbeidsoppgaveMapper.rs2arbeidsoppgave
import no.nav.syfo.api.selvbetjening.mapper.RSTiltakMapper
import no.nav.syfo.api.v2.controller.OppfolgingsplanControllerV2.Companion.METRIC_SHARE_WITH_NAV_AT_APPROVAL
import no.nav.syfo.domain.Arbeidsoppgave
import no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres
import no.nav.syfo.domain.Tiltak
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.*
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.loggInnBrukerTokenX
import no.nav.syfo.testhelper.rsTiltakLagreEksisterende
import no.nav.syfo.testhelper.rsTiltakLagreNytt
import no.nav.syfo.util.MapUtil.map
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class OppfolgingsplanControllerV2Test : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var oppfolgingsplanController: OppfolgingsplanControllerV2

    @MockBean
    lateinit var metrikk: Metrikk

    @MockBean
    lateinit var arbeidsoppgaveService: ArbeidsoppgaveService

    @MockBean
    lateinit var godkjenningService: GodkjenningService

    @MockBean
    lateinit var oppfolgingsplanService: OppfolgingsplanService

    @MockBean
    lateinit var samtykkeService: SamtykkeService

    @MockBean
    lateinit var tiltakService: TiltakService

    @Value("\${tokenx.idp}")
    private lateinit var tokenxIdp: String

    @Value("\${oppfolgingsplan.frontend.client.id}")
    private lateinit var oppfolgingsplanClientId: String

    @Before
    fun setup() {
        loggInnBrukerTokenX(contextHolder, ARBEIDSTAKER_FNR, oppfolgingsplanClientId, tokenxIdp)
    }

    @Test
    fun avbryt_som_bruker() {
        oppfolgingsplanController.avbryt(oppfolgingsplanId)
        verify(oppfolgingsplanService).avbrytPlan(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("avbryt_plan")
    }

    @Test(expected = RuntimeException::class)
    fun avbryt_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        oppfolgingsplanController.avbryt(oppfolgingsplanId)
    }

    @Test
    fun avvis_som_bruker() {
        oppfolgingsplanController.avvis(oppfolgingsplanId)
        verify(godkjenningService).avvisGodkjenning(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("avvis_plan")
    }

    @Test(expected = RuntimeException::class)
    fun avvis_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        oppfolgingsplanController.delMedNav(oppfolgingsplanId)
    }

    @Test
    fun delmedfastlege_som_bruker() {
        oppfolgingsplanController.delMedFastlege(oppfolgingsplanId)
        verify(oppfolgingsplanService).delMedFastlege(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("del_plan_med_fastlege")
    }

    @Test(expected = RuntimeException::class)
    fun delmedfastlege_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        oppfolgingsplanController.delMedFastlege(oppfolgingsplanId)
    }

    @Test
    fun delmednav_som_bruker() {
        oppfolgingsplanController.delMedNav(oppfolgingsplanId)
        verify(oppfolgingsplanService).delMedNav(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("del_plan_med_nav")
    }

    @Test(expected = RuntimeException::class)
    fun delmednav_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        oppfolgingsplanController.delMedNav(oppfolgingsplanId)
    }

    @Test
    fun godkjenn_plan_som_bruker() {
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, gyldighetstidspunkt, "true", "arbeidstaker", null)
        verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, gyldighetstidspunkt, ARBEIDSTAKER_FNR, false, false)
        verify(metrikk).tellHendelse("godkjenn_plan")
        verify(metrikk, never()).tellHendelse(METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjenn_plan_som_bruker_del_med_nav() {
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, gyldighetstidspunkt, "true", "arbeidstaker", true)
        verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, gyldighetstidspunkt, ARBEIDSTAKER_FNR, false, true)
        verify(metrikk).tellHendelse("godkjenn_plan")
        verify(metrikk).tellHendelse(METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjenn_plan_som_bruker_tvungen() {
        loggUtAlle(contextHolder)
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, gyldighetstidspunkt, "tvungenGodkjenning", "arbeidsgiver", null)
        verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, gyldighetstidspunkt, LEDER_FNR, true, false)
        verify(metrikk).tellHendelse("godkjenn_plan")
        verify(metrikk, never()).tellHendelse(METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjenn_plan_som_bruker_tvungen_del_med_nav() {
        loggUtAlle(contextHolder)
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, gyldighetstidspunkt, "tvungenGodkjenning", "arbeidsgiver", true)
        verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, gyldighetstidspunkt, LEDER_FNR, true, true)
        verify(metrikk).tellHendelse("godkjenn_plan")
        verify(metrikk).tellHendelse(METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test(expected = RuntimeException::class)
    fun godkjenn_plan_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        oppfolgingsplanController.godkjenn(oppfolgingsplanId, RSGyldighetstidspunkt(), "true", "arbeidsgiver", null)
    }

    @Test
    fun godkjennsist_plan_som_bruker() {
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        `when`(oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, ARBEIDSTAKER, ARBEIDSTAKER_FNR)).thenReturn(gyldighetstidspunkt)
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, "arbeidstaker", null)
        verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, null, ARBEIDSTAKER_FNR, false, false)
        verify(oppfolgingsplanService).hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, ARBEIDSTAKER, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("godkjenn_plan_svar")
        verify(metrikk, never()).tellHendelse(METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjennsist_plan_som_bruker_del_med_nav() {
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        `when`(oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, ARBEIDSTAKER, ARBEIDSTAKER_FNR)).thenReturn(gyldighetstidspunkt)
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, "arbeidstaker", true)
        verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, null, ARBEIDSTAKER_FNR, false, true)
        verify(oppfolgingsplanService).hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, ARBEIDSTAKER, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("godkjenn_plan_svar")
        verify(metrikk).tellHendelse(METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjennsist_plan_som_arbeidsgiver() {
        loggUtAlle(contextHolder)
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        `when`(oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, ARBEIDSGIVER, LEDER_FNR)).thenReturn(gyldighetstidspunkt)
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, "arbeidsgiver", null)
        verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, null, LEDER_FNR, false, false)
        verify(oppfolgingsplanService).hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, ARBEIDSGIVER, LEDER_FNR)
        verify(metrikk).tellHendelse("godkjenn_plan_svar")
        verify(metrikk, never()).tellHendelse(METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjennsist_plan_som_arbeidsgiver_del_med_nav() {
        loggUtAlle(contextHolder)
        loggInnBrukerTokenX(contextHolder, LEDER_FNR, oppfolgingsplanClientId, tokenxIdp)
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        `when`(oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, ARBEIDSGIVER, LEDER_FNR)).thenReturn(gyldighetstidspunkt)
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, "arbeidsgiver", true)
        verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, null, LEDER_FNR, false, true)
        verify(oppfolgingsplanService).hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, ARBEIDSGIVER, LEDER_FNR)
        verify(metrikk).tellHendelse("godkjenn_plan_svar")
        verify(metrikk).tellHendelse(METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test(expected = RuntimeException::class)
    fun godkjennsist_plan_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        oppfolgingsplanController.godkjenn(oppfolgingsplanId, "arbeidsgiver", null)
    }

    @Test
    fun kopier_som_bruker() {
        val nyPlanId = 1L
        `when`(oppfolgingsplanService.kopierOppfoelgingsdialog(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(nyPlanId)
        val res = oppfolgingsplanController.kopier(oppfolgingsplanId)
        verify(metrikk).tellHendelse("kopier_plan")
        assertEquals(res, nyPlanId)
    }

    @Test(expected = RuntimeException::class)
    fun kopier_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        oppfolgingsplanController.kopier(oppfolgingsplanId)
    }

    @Test
    fun lagrer_ny_arbeidsoppgave_som_bruker() {
        val ressursId = 1L
        val rsArbeidsoppgave = RSArbeidsoppgave()
            .arbeidsoppgavenavn("Arbeidsoppgavenavn")
            .gjennomfoering(
                RSGjennomfoering()
                    .kanGjennomfoeres(KanGjennomfoeres.TILRETTELEGGING.name)
                    .medHjelp(true)
                    .medMerTid(true)
                    .paaAnnetSted(true)
            )
        val arbeidsoppgave = map(rsArbeidsoppgave, rs2arbeidsoppgave)
        `when`(arbeidsoppgaveService.lagreArbeidsoppgave(oppfolgingsplanId, arbeidsoppgave, ARBEIDSTAKER_FNR)).thenReturn(ressursId)
        val res = oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave)
        verify(arbeidsoppgaveService).lagreArbeidsoppgave(eq(oppfolgingsplanId), any(Arbeidsoppgave::class.java), eq(ARBEIDSTAKER_FNR))
        assertEquals(res, ressursId)
    }

    @Test
    fun lagrer_eksisterende_arbeidsoppgave_som_bruker() {
        val arbeidsoppgaveId = 2L
        val rsArbeidsoppgave = RSArbeidsoppgave()
            .arbeidsoppgaveId(arbeidsoppgaveId)
            .arbeidsoppgavenavn("Arbeidsoppgavenavn")
            .gjennomfoering(
                RSGjennomfoering()
                    .kanGjennomfoeres(KanGjennomfoeres.KAN.name)
                    .kanBeskrivelse("Denne kan gjennomfoeres")
            )
        val arbeidsoppgave = map(rsArbeidsoppgave, rs2arbeidsoppgave)
        `when`(arbeidsoppgaveService.lagreArbeidsoppgave(oppfolgingsplanId, arbeidsoppgave, ARBEIDSTAKER_FNR)).thenReturn(arbeidsoppgaveId)
        val res = oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave)
        verify(arbeidsoppgaveService).lagreArbeidsoppgave(eq(oppfolgingsplanId), any(Arbeidsoppgave::class.java), eq(ARBEIDSTAKER_FNR))
        assertEquals(res, arbeidsoppgaveId)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_lagre_arbeidsoppgave() {
        loggUtAlle(contextHolder)
        val rsArbeidsoppgave = RSArbeidsoppgave()
        oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave)
    }

    @Test
    fun lagrer_tiltak_ny_som_bruker() {
        val ressursId = 1L
        val rsTiltak = rsTiltakLagreNytt()
        val tiltak = map(rsTiltak, RSTiltakMapper.rs2tiltak)
        `when`(tiltakService.lagreTiltak(oppfolgingsplanId, tiltak, ARBEIDSTAKER_FNR)).thenReturn(ressursId)
        val res = oppfolgingsplanController.lagreTiltak(oppfolgingsplanId, rsTiltak)
        verify(tiltakService).lagreTiltak(eq(oppfolgingsplanId), any(Tiltak::class.java), eq(ARBEIDSTAKER_FNR))
        assertEquals(res, ressursId)
    }

    @Test
    fun lagre_tiltak_eksisterende_som_bruker() {
        val ressursId = 2L
        val rsTiltak = rsTiltakLagreEksisterende()
        val tiltak = map(rsTiltak, RSTiltakMapper.rs2tiltak)
        `when`(tiltakService.lagreTiltak(oppfolgingsplanId, tiltak, ARBEIDSTAKER_FNR)).thenReturn(ressursId)
        val res = oppfolgingsplanController.lagreTiltak(oppfolgingsplanId, rsTiltak)
        verify(tiltakService).lagreTiltak(eq(oppfolgingsplanId), any(Tiltak::class.java), eq(ARBEIDSTAKER_FNR))
        assertEquals(res, ressursId)
    }

    @Test(expected = RuntimeException::class)
    fun lagre_tiltak_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        val rsTiltak = RSTiltak()
        oppfolgingsplanController.lagreTiltak(oppfolgingsplanId, rsTiltak)
    }

    @Test
    fun nullstill_godkjenning_som_bruker() {
        oppfolgingsplanController.nullstillGodkjenning(oppfolgingsplanId)
        verify(oppfolgingsplanService).nullstillGodkjenning(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("nullstill_godkjenning")
    }

    @Test
    fun sett_som_bruker() {
        oppfolgingsplanController.sett(oppfolgingsplanId)
        verify(oppfolgingsplanService).oppdaterSistInnlogget(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        verify(metrikk).tellHendelse("sett_plan")
    }

    @Test(expected = RuntimeException::class)
    fun sett_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        oppfolgingsplanController.sett(oppfolgingsplanId)
    }

    @Test
    fun samtykk_som_bruker() {
        oppfolgingsplanController.samtykk(oppfolgingsplanId, true)
        verify(samtykkeService).giSamtykke(oppfolgingsplanId, ARBEIDSTAKER_FNR, true)
        verify(metrikk).tellHendelse("samtykk_plan")
    }

    @Test(expected = RuntimeException::class)
    fun samtykk_ikke_innlogget_bruker() {
        loggUtAlle(contextHolder)
        oppfolgingsplanController.samtykk(oppfolgingsplanId, true)
    }

    companion object {
        private const val oppfolgingsplanId = 1L
    }
}