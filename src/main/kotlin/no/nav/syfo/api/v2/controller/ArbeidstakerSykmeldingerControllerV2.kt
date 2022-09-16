package no.nav.syfo.api.selvbetjening.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.oidc.OIDCUtil.getIssuerToken
import no.nav.syfo.sykmeldinger.ArbeidstakerSykmeldingerConsumer
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import no.nav.syfo.tokenx.tokendings.TokenDingsConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v2/arbeidstaker/sykmeldinger"])
class ArbeidstakerSykmeldingerControllerV2 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val metrikk: Metrikk,
    private val aktorregisterConsumer: AktorregisterConsumer,
    private val arbeidstakerSykmeldingerConsumer: ArbeidstakerSykmeldingerConsumer,
    private val tokenDingsConsumer: TokenDingsConsumer,
    @Value("\${tokenx.idp}")
    val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    val oppfolgingsplanClientId: String,
    @Value("\${syfosmregister.id}")
    private val targetApp: String? = null
) {
    @ResponseBody
    @GetMapping
    fun getSendteSykmeldinger(@RequestParam(required = false) today: String?): ResponseEntity<List<Sykmelding>> {
        metrikk.tellHendelse("get_sykmeldinger")
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val issuerToken = getIssuerToken(contextHolder, TOKENX)
        val exchangedToken = tokenDingsConsumer.exchangeToken(issuerToken, targetApp!!)
        val bearerToken = "Bearer $exchangedToken"

        val oppslattIdentAktorId = aktorregisterConsumer.hentAktorIdForFnr(innloggetIdent)
        val isTodayPresent = today.toBoolean()
        val sendteSykmeldinger = arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(oppslattIdentAktorId, bearerToken, isTodayPresent)
        return sendteSykmeldinger.map { sykmeldinger: List<Sykmelding> ->
            ResponseEntity
                .status(HttpStatus.OK)
                .body(sykmeldinger)
        }.orElseGet {
            ResponseEntity
                .status(HttpStatus.OK)
                .body(java.util.List.of())
        }
    }
}
