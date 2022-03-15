package no.nav.syfo.api.v2.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.api.v2.domain.Virksomhet
import no.nav.syfo.ereg.EregConsumer
import no.nav.syfo.oidc.OIDCIssuer
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject


@RestController
@ProtectedWithClaims(issuer = OIDCIssuer.EKSTERN)
@RequestMapping(value = ["/api/v2/virksomhet/{virksomhetsnummer}"])
class VirksomhetControllerV2 @Inject constructor(
    private val eregConsumer: EregConsumer,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getVirksomhet(
        @PathVariable("virksomhetsnummer") virksomhetsnummer: String
    ): ResponseEntity<Virksomhet> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(Virksomhet(
                virksomhetsnummer = virksomhetsnummer,
                navn = eregConsumer.virksomhetsnavn(virksomhetsnummer)
            ))
    }
}
