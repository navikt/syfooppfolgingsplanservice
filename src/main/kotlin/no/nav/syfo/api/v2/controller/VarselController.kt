package no.nav.syfo.api.v2.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.v2.domain.GodkjennPlanVarsel
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.EsyfovarselService
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TokenXUtil.TokenXIssuer.TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v2/varsel"])
class VarselController @Inject constructor(
    private val metrikk: Metrikk,
    private val contextHolder: TokenValidationContextHolder,
    @Value("\${tokenx.idp}")
    private val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
    private val esyfovarselService: EsyfovarselService,
) {
    private val log = LoggerFactory.getLogger("no.nav.syfo.api.v2.controller.VarselController")
    @PostMapping(path = ["/ferdigstill"])
    fun ferdigstillVarsel(
        @RequestBody godkjennPlanVarsel: GodkjennPlanVarsel
    ) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        esyfovarselService.ferdigstillVarsel(innloggetIdent, godkjennPlanVarsel)
        metrikk.tellHendelse("call_ferdigstillVarsel")
    }
}
