package no.nav.syfo.api.v2.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.v2.domain.oppfolgingsplan.ArbeidsoppgaveRequest
import no.nav.syfo.api.v2.domain.oppfolgingsplan.Gyldighetstidspunkt
import no.nav.syfo.api.v2.domain.oppfolgingsplan.TiltakRequest
import no.nav.syfo.api.v2.mapper.toArbeidsoppgave
import no.nav.syfo.api.v2.mapper.toGyldighetstidspunkt
import no.nav.syfo.api.v2.mapper.toTiltak
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.*
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.*
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4", "acr=idporten-loa-high"], combineWithOr = true)
@RequestMapping(value = ["/api/v2/oppfolgingsplan/actions/{id}"])
class OppfolgingsplanControllerV2 @Inject constructor(
    private val metrikk: Metrikk,
    private val contextHolder: TokenValidationContextHolder,
    private val arbeidsoppgaveService: ArbeidsoppgaveService,
    private val godkjenningService: GodkjenningService,
    private val oppfolgingsplanService: OppfolgingsplanService,
    private val samtykkeService: SamtykkeService,
    private val tiltakService: TiltakService,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {

    @PostMapping(path = ["/avbryt"])
    fun avbryt(@PathVariable("id") id: Long): Long {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val newId = oppfolgingsplanService.avbrytPlan(id, innloggetIdent)
        metrikk.tellHendelse("avbryt_plan")
        return newId
    }

    @PostMapping(path = ["/avvis"])
    fun avvis(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        godkjenningService.avvisGodkjenning(id, innloggetIdent)
        metrikk.tellHendelse("avvis_plan")
    }

    @PostMapping(path = ["/delmedfastlege"])
    fun delMedFastlege(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        oppfolgingsplanService.delMedFastlege(id, innloggetIdent)
        metrikk.tellHendelse("del_plan_med_fastlege")
    }

    @PostMapping(path = ["/delmednav"])
    fun delMedNav(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        oppfolgingsplanService.delMedNav(id, innloggetIdent)
        metrikk.tellHendelse("del_plan_med_nav")
    }

    @PostMapping(path = ["/godkjenn"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun godkjenn(
        @PathVariable("id") id: Long,
        @RequestBody gyldighetstidspunkt: Gyldighetstidspunkt,
        @RequestParam("status") status: String,
        @RequestParam("aktoer") aktor: String,
        @RequestParam(value = "delmednav", required = false) delMedNav: Boolean?,
    ) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val isPlanSharedWithNAV = Optional.ofNullable(delMedNav).orElse(false)
        if (isPlanSharedWithNAV) {
            countShareWithNAVAtApproval()
        }
        val tvungenGodkjenning = status == "tvungenGodkjenning"
        godkjenningService.godkjennOppfolgingsplan(
            id,
            gyldighetstidspunkt.toGyldighetstidspunkt(),
            innloggetIdent,
            tvungenGodkjenning,
            isPlanSharedWithNAV,
        )
        metrikk.tellHendelse("godkjenn_plan")
    }

    @PostMapping(path = ["/egenarbedsgiver/godkjenn"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun godkjennEgenPlanArbeidsgiver(
        @PathVariable("id") id: Long,
        @RequestBody gyldighetstidspunkt: Gyldighetstidspunkt,
        @RequestParam(value = "delmednav", required = false) delMedNav: Boolean?,
    ) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value

        val isPlanSharedWithNAV = Optional.ofNullable(delMedNav).orElse(false)
        if (isPlanSharedWithNAV) {
            countShareWithNAVAtApproval()
        }

        godkjenningService.godkjennLederSinEgenOppfolgingsplan(id, gyldighetstidspunkt.toGyldighetstidspunkt(), innloggetIdent, isPlanSharedWithNAV)

        metrikk.tellHendelse("godkjenn_plan")
        metrikk.tellHendelse("godkjenn_plan_egen_leder")
    }

    @PostMapping(path = ["/godkjennsist"], produces = [APPLICATION_JSON_VALUE])
    fun godkjenn(
        @PathVariable("id") id: Long,
        @RequestParam("aktoer") aktor: String,
        @RequestParam(value = "delmednav", required = false) delMedNav: Boolean?,
    ) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val isPlanSharedWithNAV = Optional.ofNullable(delMedNav).orElse(false)
        if (isPlanSharedWithNAV) {
            countShareWithNAVAtApproval()
        }
        godkjenningService.godkjennOppfolgingsplan(id, null, innloggetIdent, false, isPlanSharedWithNAV)
        metrikk.tellHendelse("godkjenn_plan_svar")
    }

    @PostMapping(path = ["/kopier"], produces = [APPLICATION_JSON_VALUE])
    fun kopier(@PathVariable("id") id: Long): Long {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val nyPlanId = oppfolgingsplanService.kopierOppfoelgingsdialog(id, innloggetIdent)
        metrikk.tellHendelse("kopier_plan")
        return nyPlanId
    }

    @PostMapping(
        path = ["/lagreArbeidsoppgave"],
        consumes = [APPLICATION_JSON_VALUE],
        produces = [APPLICATION_JSON_VALUE],
    )
    fun lagreArbeidsoppgave(
        @PathVariable("id") id: Long,
        @RequestBody arbeidsoppgaveRequest: ArbeidsoppgaveRequest,
    ): Long {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val arbeidsoppgave = arbeidsoppgaveRequest.toArbeidsoppgave()
        return arbeidsoppgaveService.lagreArbeidsoppgave(id, arbeidsoppgave, innloggetIdent)
    }

    @PostMapping(path = ["/lagreTiltak"], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun lagreTiltak(
        @PathVariable("id") id: Long,
        @RequestBody tiltakRequest: TiltakRequest,
    ): Long {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val tiltak = tiltakRequest.toTiltak()
        return tiltakService.lagreTiltak(id, tiltak, innloggetIdent)
    }

    @PostMapping(path = ["/nullstillGodkjenning"])
    fun nullstillGodkjenning(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        oppfolgingsplanService.nullstillGodkjenning(id, innloggetIdent)
        metrikk.tellHendelse("nullstill_godkjenning")
    }

    @PostMapping(path = ["/samtykk"])
    fun samtykk(
        @PathVariable("id") id: Long,
        @RequestParam("samtykke") samtykke: Boolean,
    ) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        samtykkeService.giSamtykke(id, innloggetIdent, samtykke)
        metrikk.tellHendelse("samtykk_plan")
    }

    @PostMapping(path = ["/sett"])
    fun sett(@PathVariable("id") id: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
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
