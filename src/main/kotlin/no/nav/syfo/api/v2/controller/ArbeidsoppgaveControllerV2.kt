package no.nav.syfo.api.v2.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.ArbeidsoppgaveService
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v2/arbeidsoppgave/actions/{arbeidsoppgaveId}"])
class ArbeidsoppgaveControllerV2 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val arbeidsoppgaveService: ArbeidsoppgaveService,
    private val metrikk: Metrikk,
    @Value("\${tokenx.idp}")
    private val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {
    @PostMapping(path = ["/slett"])
    fun slettArbeidsoppgave(@PathVariable("arbeidsoppgaveId") arbeidsoppgaveId: Long) {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        arbeidsoppgaveService.slettArbeidsoppgave(arbeidsoppgaveId, innloggetIdent)
        metrikk.tellHendelse("slett_arbeidsoppgave")
    }
}