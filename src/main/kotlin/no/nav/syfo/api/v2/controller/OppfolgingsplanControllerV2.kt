package no.nav.syfo.api.v2.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSGIVER
import no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSTAKER
import no.nav.syfo.api.selvbetjening.domain.RSArbeidsoppgave
import no.nav.syfo.api.selvbetjening.domain.RSGyldighetstidspunkt
import no.nav.syfo.api.selvbetjening.domain.RSTiltak
import no.nav.syfo.api.selvbetjening.mapper.RSArbeidsoppgaveMapper.rs2arbeidsoppgave
import no.nav.syfo.api.selvbetjening.mapper.RSTiltakMapper.rs2tiltak
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.*
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import no.nav.syfo.util.MapUtil.map
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v2/oppfolgingsplan/actions/{id}"])
class OppfolgingsplanControllerV2 @Inject constructor(
    private val metrikk: Metrikk,
    private val contextHolder: TokenValidationContextHolder,
    private val arbeidsoppgaveService: ArbeidsoppgaveService,
    private val godkjenningService: GodkjenningService,
    private val oppfolgingsplanService: OppfolgingsplanService,
    private val samtykkeService: SamtykkeService,
    private val tiltakService: TiltakService,
    @Value("\${tokenx.idp}")
    private val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {

    @PostMapping(path = ["/avbryt"])
    fun avbryt(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        oppfolgingsplanService.avbrytPlan(id, innloggetIdent)
        metrikk.tellHendelse("avbryt_plan")
    }

    @PostMapping(path = ["/avvis"])
    fun avvis(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        godkjenningService.avvisGodkjenning(id, innloggetIdent)
        metrikk.tellHendelse("avvis_plan")
    }

    @PostMapping(path = ["/delmedfastlege"])
    fun delMedFastlege(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId, "dev-gcp:plattformsikkerhet:debug-dings")
            .fnrFromIdportenTokenX()
            .value
        oppfolgingsplanService.delMedFastlege(id, innloggetIdent)
        metrikk.tellHendelse("del_plan_med_fastlege")
    }

    @PostMapping(path = ["/delmednav"])
    fun delMedNav(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        oppfolgingsplanService.delMedNav(id, innloggetIdent)
        metrikk.tellHendelse("del_plan_med_nav")
    }

    @PostMapping(path = ["/godkjenn"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun godkjenn(
        @PathVariable("id") id: Long,
        @RequestBody rsGyldighetstidspunkt: RSGyldighetstidspunkt,
        @RequestParam("status") status: String,
        @RequestParam("aktoer") aktor: String,
        @RequestParam(value = "delmednav", required = false) delMedNav: Boolean?
    ): RSGyldighetstidspunkt {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val isPlanSharedWithNAV = Optional.ofNullable(delMedNav).orElse(false)
        if (isPlanSharedWithNAV) {
            countShareWithNAVAtApproval()
        }
        val tvungenGodkjenning = status == "tvungenGodkjenning"
        godkjenningService.godkjennOppfolgingsplan(id, rsGyldighetstidspunkt, innloggetIdent, tvungenGodkjenning, isPlanSharedWithNAV)
        metrikk.tellHendelse("godkjenn_plan")
        return rsGyldighetstidspunkt
    }

    @PostMapping(path = ["/godkjennsist"], produces = [APPLICATION_JSON_VALUE])
    fun godkjenn(
        @PathVariable("id") id: Long,
        @RequestParam("aktoer") aktor: String,
        @RequestParam(value = "delmednav", required = false) delMedNav: Boolean?
    ): RSGyldighetstidspunkt {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val isPlanSharedWithNAV = Optional.ofNullable(delMedNav).orElse(false)
        if (isPlanSharedWithNAV) {
            countShareWithNAVAtApproval()
        }
        godkjenningService.godkjennOppfolgingsplan(id, null, innloggetIdent, false, isPlanSharedWithNAV)
        metrikk.tellHendelse("godkjenn_plan_svar")
        return hentGyldighetstidspunktForPlan(id, aktor, innloggetIdent)
    }

    private fun hentGyldighetstidspunktForPlan(@PathVariable("id") id: Long, @RequestParam("aktoer") aktor: String, innloggetIdent: String): RSGyldighetstidspunkt {
        return if ("arbeidsgiver" == aktor) {
            oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(id, ARBEIDSGIVER, innloggetIdent)
        } else {
            oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(id, ARBEIDSTAKER, innloggetIdent)
        }
    }

    @PostMapping(path = ["/kopier"], produces = [APPLICATION_JSON_VALUE])
    fun kopier(@PathVariable("id") id: Long): Long {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val nyPlanId = oppfolgingsplanService.kopierOppfoelgingsdialog(id, innloggetIdent)
        metrikk.tellHendelse("kopier_plan")
        return nyPlanId
    }

    @PostMapping(path = ["/lagreArbeidsoppgave"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun lagreArbeidsoppgave(
        @PathVariable("id") id: Long,
        @RequestBody rsArbeidsoppgave: RSArbeidsoppgave
    ): Long {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val arbeidsoppgave = map(rsArbeidsoppgave, rs2arbeidsoppgave)
        return arbeidsoppgaveService.lagreArbeidsoppgave(id, arbeidsoppgave, innloggetIdent)
    }

    @PostMapping(path = ["/lagreTiltak"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun lagreTiltak(
        @PathVariable("id") id: Long,
        @RequestBody rsTiltak: RSTiltak
    ): Long {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val tiltak = map(rsTiltak, rs2tiltak)
        return tiltakService.lagreTiltak(id, tiltak, innloggetIdent)
    }

    @PostMapping(path = ["/nullstillGodkjenning"])
    fun nullstillGodkjenning(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        oppfolgingsplanService.nullstillGodkjenning(id, innloggetIdent)
        metrikk.tellHendelse("nullstill_godkjenning")
    }

    @PostMapping(path = ["/samtykk"])
    fun samtykk(
        @PathVariable("id") id: Long,
        @RequestParam("samtykke") samtykke: Boolean
    ) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        samtykkeService.giSamtykke(id, innloggetIdent, samtykke)
        metrikk.tellHendelse("samtykk_plan")
    }

    @PostMapping(path = ["/sett"])
    fun sett(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        oppfolgingsplanService.oppdaterSistInnlogget(id, innloggetIdent)
        metrikk.tellHendelse("sett_plan")
    }

    private fun countShareWithNAVAtApproval() {
        metrikk.tellHendelse(METRIC_SHARE_WITH_NAV_AT_APPROVAL)
    }

    companion object {
        const val METRIC_SHARE_WITH_NAV_AT_APPROVAL = "del_plan_med_nav_ved_godkjenning"
    }

}