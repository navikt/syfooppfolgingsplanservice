package no.nav.syfo.api.v3.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.v2.domain.Virksomhet
import no.nav.syfo.ereg.EregConsumer
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject


@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v3/virksomhet/{virksomhetsnummer}"])
class VirksomhetControllerV3 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val eregConsumer: EregConsumer,
    @Value("\${tokenx.idp}")
    private val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getVirksomhet(
        @PathVariable("virksomhetsnummer") virksomhetsnummer: String
    ): ResponseEntity<Virksomhet> {
        TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                Virksomhet(
                    virksomhetsnummer = virksomhetsnummer,
                    navn = eregConsumer.virksomhetsnavn(virksomhetsnummer)
                )
            )
    }
}
