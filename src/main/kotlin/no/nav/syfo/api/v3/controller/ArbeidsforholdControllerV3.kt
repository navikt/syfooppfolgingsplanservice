package no.nav.syfo.api.v3.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.v3.domain.Arbeidsforhold
import no.nav.syfo.api.v3.domain.mapToArbeidsforhold
import no.nav.syfo.service.ArbeidsforholdService
import no.nav.syfo.service.BrukertilgangService
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject


@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4", "acr=idporten-loa-high"], combineWithOr = true)
@RequestMapping(value = ["/api/v3/arbeidsforhold"])
class ArbeidsforholdControllerV3 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val arbeidsforholdService: ArbeidsforholdService,
    private val brukertilgangService: BrukertilgangService,
    @Value("\${tokenx.idp}")
    private val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getArbeidsforhold(
        @RequestParam("fnr") fnr: String,
        @RequestParam("virksomhetsnummer") virksomhetsnummer: String,
    ): ResponseEntity<List<Arbeidsforhold>> {

        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value

        if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, fnr)) {
            LOG.error("Ikke tilgang til arbeidsforhold: Bruker spør om noen andre enn seg selv eller egne ansatte")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build()
        }
        val arbeidsforhold = arbeidsforholdService.arbeidstakersStillingerForOrgnummer(fnr, listOf(virksomhetsnummer))
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(arbeidsforhold.map { it.mapToArbeidsforhold() })
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(ArbeidsforholdControllerV3::class.java)
    }
}
