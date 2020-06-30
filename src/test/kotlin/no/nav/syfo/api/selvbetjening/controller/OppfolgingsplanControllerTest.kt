package no.nav.syfo.api.selvbetjening.controller

import no.nav.syfo.api.AbstractRessursTilgangTest
import no.nav.syfo.api.selvbetjening.domain.*
import no.nav.syfo.api.selvbetjening.mapper.RSArbeidsoppgaveMapper
import no.nav.syfo.api.selvbetjening.mapper.RSTiltakMapper
import no.nav.syfo.domain.Arbeidsoppgave
import no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres
import no.nav.syfo.domain.Tiltak
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.*
import no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker
import no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle
import no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR
import no.nav.syfo.testhelper.UserConstants.LEDER_FNR
import no.nav.syfo.testhelper.rsTiltakLagreEksisterende
import no.nav.syfo.testhelper.rsTiltakLagreNytt
import no.nav.syfo.util.MapUtil
import org.junit.*
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import javax.inject.Inject

class OppfolgingsplanControllerTest : AbstractRessursTilgangTest() {
    @Inject
    private lateinit var oppfolgingsplanController: OppfolgingsplanController

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

    @Before
    fun setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR)
    }

    @Test
    fun avbryt_som_bruker() {
        oppfolgingsplanController.avbryt(oppfolgingsplanId)
        Mockito.verify(oppfolgingsplanService).avbrytPlan(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("avbryt_plan")
    }

    @Test(expected = RuntimeException::class)
    fun avbryt_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.avbryt(oppfolgingsplanId)
    }

    @Test
    fun avvis_som_bruker() {
        oppfolgingsplanController.avvis(oppfolgingsplanId)
        Mockito.verify(godkjenningService).avvisGodkjenning(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("avvis_plan")
    }

    @Test(expected = RuntimeException::class)
    fun avvis_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.delMedNav(oppfolgingsplanId)
    }

    @Test
    fun delmedfastlege_som_bruker() {
        oppfolgingsplanController.delMedFastlege(oppfolgingsplanId)
        Mockito.verify(oppfolgingsplanService).delMedFastlege(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("del_plan_med_fastlege")
    }

    @Test(expected = RuntimeException::class)
    fun delmedfastlege_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.delMedFastlege(oppfolgingsplanId)
    }

    @Test
    fun delmednav_som_bruker() {
        oppfolgingsplanController.delMedNav(oppfolgingsplanId)
        Mockito.verify(oppfolgingsplanService).delMedNav(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("del_plan_med_nav")
    }

    @Test(expected = RuntimeException::class)
    fun delmednav_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.delMedNav(oppfolgingsplanId)
    }

    @Test
    fun godkjenn_plan_som_bruker() {
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, gyldighetstidspunkt, "true", "arbeidstaker", null)
        Mockito.verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, gyldighetstidspunkt, ARBEIDSTAKER_FNR, false, false)
        Mockito.verify(metrikk).tellHendelse("godkjenn_plan")
        Mockito.verify(metrikk, Mockito.never()).tellHendelse(OppfolgingsplanController.METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        Assert.assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjenn_plan_som_bruker_del_med_nav() {
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, gyldighetstidspunkt, "true", "arbeidstaker", true)
        Mockito.verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, gyldighetstidspunkt, ARBEIDSTAKER_FNR, false, true)
        Mockito.verify(metrikk).tellHendelse("godkjenn_plan")
        Mockito.verify(metrikk).tellHendelse(OppfolgingsplanController.METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        Assert.assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjenn_plan_som_bruker_tvungen() {
        loggUtAlle(oidcRequestContextHolder)
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR)
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, gyldighetstidspunkt, "tvungenGodkjenning", "arbeidsgiver", null)
        Mockito.verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, gyldighetstidspunkt, LEDER_FNR, true, false)
        Mockito.verify(metrikk).tellHendelse("godkjenn_plan")
        Mockito.verify(metrikk, Mockito.never()).tellHendelse(OppfolgingsplanController.METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        Assert.assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjenn_plan_som_bruker_tvungen_del_med_nav() {
        loggUtAlle(oidcRequestContextHolder)
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR)
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, gyldighetstidspunkt, "tvungenGodkjenning", "arbeidsgiver", true)
        Mockito.verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, gyldighetstidspunkt, LEDER_FNR, true, true)
        Mockito.verify(metrikk).tellHendelse("godkjenn_plan")
        Mockito.verify(metrikk).tellHendelse(OppfolgingsplanController.METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        Assert.assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test(expected = RuntimeException::class)
    fun godkjenn_plan_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.godkjenn(oppfolgingsplanId, RSGyldighetstidspunkt(), "true", "arbeidsgiver", null)
    }

    @Test
    fun godkjennsist_plan_som_bruker() {
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        Mockito.`when`(oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, BrukerkontekstConstant.ARBEIDSTAKER, ARBEIDSTAKER_FNR)).thenReturn(gyldighetstidspunkt)
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, "true", "arbeidstaker", null)
        Mockito.verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, null, ARBEIDSTAKER_FNR, false, false)
        Mockito.verify(oppfolgingsplanService).hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, BrukerkontekstConstant.ARBEIDSTAKER, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("godkjenn_plan_svar")
        Mockito.verify(metrikk, Mockito.never()).tellHendelse(OppfolgingsplanController.METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        Assert.assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjennsist_plan_som_bruker_del_med_nav() {
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        Mockito.`when`(oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, BrukerkontekstConstant.ARBEIDSTAKER, ARBEIDSTAKER_FNR)).thenReturn(gyldighetstidspunkt)
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, "true", "arbeidstaker", true)
        Mockito.verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, null, ARBEIDSTAKER_FNR, false, true)
        Mockito.verify(oppfolgingsplanService).hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, BrukerkontekstConstant.ARBEIDSTAKER, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("godkjenn_plan_svar")
        Mockito.verify(metrikk).tellHendelse(OppfolgingsplanController.METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        Assert.assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjennsist_plan_som_arbeidsgiver() {
        loggUtAlle(oidcRequestContextHolder)
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR)
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        Mockito.`when`(oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, BrukerkontekstConstant.ARBEIDSGIVER, LEDER_FNR)).thenReturn(gyldighetstidspunkt)
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, "true", "arbeidsgiver", null)
        Mockito.verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, null, LEDER_FNR, false, false)
        Mockito.verify(oppfolgingsplanService).hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, BrukerkontekstConstant.ARBEIDSGIVER, LEDER_FNR)
        Mockito.verify(metrikk).tellHendelse("godkjenn_plan_svar")
        Mockito.verify(metrikk, Mockito.never()).tellHendelse(OppfolgingsplanController.METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        Assert.assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test
    fun godkjennsist_plan_som_arbeidsgiver_del_med_nav() {
        loggUtAlle(oidcRequestContextHolder)
        loggInnBruker(oidcRequestContextHolder, LEDER_FNR)
        val gyldighetstidspunkt = RSGyldighetstidspunkt()
        Mockito.`when`(oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, BrukerkontekstConstant.ARBEIDSGIVER, LEDER_FNR)).thenReturn(gyldighetstidspunkt)
        val rsGyldighetstidspunkt = oppfolgingsplanController.godkjenn(oppfolgingsplanId, "true", "arbeidsgiver", true)
        Mockito.verify(godkjenningService).godkjennOppfolgingsplan(oppfolgingsplanId, null, LEDER_FNR, false, true)
        Mockito.verify(oppfolgingsplanService).hentGyldighetstidspunktForGodkjentPlan(oppfolgingsplanId, BrukerkontekstConstant.ARBEIDSGIVER, LEDER_FNR)
        Mockito.verify(metrikk).tellHendelse("godkjenn_plan_svar")
        Mockito.verify(metrikk).tellHendelse(OppfolgingsplanController.METRIC_SHARE_WITH_NAV_AT_APPROVAL)
        Assert.assertEquals(gyldighetstidspunkt, rsGyldighetstidspunkt)
    }

    @Test(expected = RuntimeException::class)
    fun godkjennsist_plan_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.godkjenn(oppfolgingsplanId, "true", "arbeidsgiver", null)
    }

    @Test
    fun kopier_som_bruker() {
        val nyPlanId = 1L
        Mockito.`when`(oppfolgingsplanService.kopierOppfoelgingsdialog(oppfolgingsplanId, ARBEIDSTAKER_FNR)).thenReturn(nyPlanId)
        val res = oppfolgingsplanController.kopier(oppfolgingsplanId)
        Mockito.verify(metrikk).tellHendelse("kopier_plan")
        Assert.assertEquals(res, nyPlanId)
    }

    @Test(expected = RuntimeException::class)
    fun kopier_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.kopier(oppfolgingsplanId)
    }

    @Test
    fun lagrer_ny_arbeidsoppgave_som_bruker() {
        val ressursId = 1L
        val rsArbeidsoppgave = RSArbeidsoppgave()
            .arbeidsoppgavenavn("Arbeidsoppgavenavn")
            .gjennomfoering(RSGjennomfoering()
                .kanGjennomfoeres(KanGjennomfoeres.TILRETTELEGGING.name)
                .medHjelp(true)
                .medMerTid(true)
                .paaAnnetSted(true)
            )
        val arbeidsoppgave = MapUtil.map(rsArbeidsoppgave, RSArbeidsoppgaveMapper.rs2arbeidsoppgave)
        Mockito.`when`(arbeidsoppgaveService.lagreArbeidsoppgave(oppfolgingsplanId, arbeidsoppgave, ARBEIDSTAKER_FNR)).thenReturn(ressursId)
        val res = oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave)
        Mockito.verify(arbeidsoppgaveService).lagreArbeidsoppgave(ArgumentMatchers.eq<Long>(oppfolgingsplanId), ArgumentMatchers.any(Arbeidsoppgave::class.java), ArgumentMatchers.eq<String>(ARBEIDSTAKER_FNR))
        Assert.assertEquals(res, ressursId)
    }

    @Test
    fun lagrer_eksisterende_arbeidsoppgave_som_bruker() {
        val arbeidsoppgaveId = 2L
        val rsArbeidsoppgave = RSArbeidsoppgave()
            .arbeidsoppgaveId(arbeidsoppgaveId)
            .arbeidsoppgavenavn("Arbeidsoppgavenavn")
            .gjennomfoering(RSGjennomfoering()
                .kanGjennomfoeres(KanGjennomfoeres.KAN.name)
                .kanBeskrivelse("Denne kan gjennomfoeres")
            )
        val arbeidsoppgave = MapUtil.map(rsArbeidsoppgave, RSArbeidsoppgaveMapper.rs2arbeidsoppgave)
        Mockito.`when`(arbeidsoppgaveService.lagreArbeidsoppgave(oppfolgingsplanId, arbeidsoppgave, ARBEIDSTAKER_FNR)).thenReturn(arbeidsoppgaveId)
        val res = oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave)
        Mockito.verify(arbeidsoppgaveService).lagreArbeidsoppgave(ArgumentMatchers.eq<Long>(oppfolgingsplanId), ArgumentMatchers.any(Arbeidsoppgave::class.java), ArgumentMatchers.eq<String>(ARBEIDSTAKER_FNR))
        Assert.assertEquals(res, arbeidsoppgaveId)
    }

    @Test(expected = RuntimeException::class)
    fun finner_ikke_innlogget_bruker_lagre_arbeidsoppgave() {
        loggUtAlle(oidcRequestContextHolder)
        val rsArbeidsoppgave = RSArbeidsoppgave()
        oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave)
    }

    @Test
    fun lagrer_tiltak_ny_som_bruker() {
        val ressursId = 1L
        val rsTiltak = rsTiltakLagreNytt()
        val tiltak = MapUtil.map(rsTiltak, RSTiltakMapper.rs2tiltak)
        Mockito.`when`(tiltakService.lagreTiltak(oppfolgingsplanId, tiltak, ARBEIDSTAKER_FNR)).thenReturn(ressursId)
        val res = oppfolgingsplanController.lagreTiltak(oppfolgingsplanId, rsTiltak)
        Mockito.verify(tiltakService).lagreTiltak(ArgumentMatchers.eq<Long>(oppfolgingsplanId), ArgumentMatchers.any(Tiltak::class.java), ArgumentMatchers.eq<String>(ARBEIDSTAKER_FNR))
        Assert.assertEquals(res, ressursId)
    }

    @Test
    fun lagre_tiltak_eksisterende_som_bruker() {
        val ressursId = 2L
        val rsTiltak = rsTiltakLagreEksisterende()
        val tiltak = MapUtil.map(rsTiltak, RSTiltakMapper.rs2tiltak)
        Mockito.`when`(tiltakService.lagreTiltak(oppfolgingsplanId, tiltak, ARBEIDSTAKER_FNR)).thenReturn(ressursId)
        val res = oppfolgingsplanController.lagreTiltak(oppfolgingsplanId, rsTiltak)
        Mockito.verify(tiltakService).lagreTiltak(ArgumentMatchers.eq<Long>(oppfolgingsplanId), ArgumentMatchers.any(Tiltak::class.java), ArgumentMatchers.eq<String>(ARBEIDSTAKER_FNR))
        Assert.assertEquals(res, ressursId)
    }

    @Test(expected = RuntimeException::class)
    fun lagre_tiltak_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        val rsTiltak = RSTiltak()
        oppfolgingsplanController.lagreTiltak(oppfolgingsplanId, rsTiltak)
    }

    @Test
    fun forespor_revidering_eksisterende_som_bruker() {
        oppfolgingsplanController.foresporRevidering(oppfolgingsplanId)
        Mockito.verify(oppfolgingsplanService).foresporRevidering(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("forespor_revidering")
    }

    @Test(expected = RuntimeException::class)
    fun forespor_revidering_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.foresporRevidering(oppfolgingsplanId)
    }

    @Test
    fun nullstill_godkjenning_som_bruker() {
        oppfolgingsplanController.nullstillGodkjenning(oppfolgingsplanId)
        Mockito.verify(oppfolgingsplanService).nullstillGodkjenning(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("nullstill_godkjenning")
    }

    @Test(expected = RuntimeException::class)
    fun nullstill_godkjenning_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.foresporRevidering(oppfolgingsplanId)
    }

    @Test
    fun sett_som_bruker() {
        oppfolgingsplanController.sett(oppfolgingsplanId)
        Mockito.verify(oppfolgingsplanService).oppdaterSistInnlogget(oppfolgingsplanId, ARBEIDSTAKER_FNR)
        Mockito.verify(metrikk).tellHendelse("sett_plan")
    }

    @Test(expected = RuntimeException::class)
    fun sett_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.sett(oppfolgingsplanId)
    }

    @Test
    fun samtykk_som_bruker() {
        oppfolgingsplanController.samtykk(oppfolgingsplanId, true)
        Mockito.verify(samtykkeService).giSamtykke(oppfolgingsplanId, ARBEIDSTAKER_FNR, true)
        Mockito.verify(metrikk).tellHendelse("samtykk_plan")
    }

    @Test(expected = RuntimeException::class)
    fun samtykk_ikke_innlogget_bruker() {
        loggUtAlle(oidcRequestContextHolder)
        oppfolgingsplanController.samtykk(oppfolgingsplanId, true)
    }

    companion object {
        private const val oppfolgingsplanId = 1L
    }
}
