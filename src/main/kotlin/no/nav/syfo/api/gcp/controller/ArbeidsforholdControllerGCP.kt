package no.nav.syfo.api.gcp.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.aareg.AaregConsumer
import no.nav.syfo.api.gcp.domain.ArbeidsforholdGCP
import no.nav.syfo.api.gcp.domain.mapToArbeidsforhold
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.oidc.OIDCUtil
import no.nav.syfo.service.BrukertilgangService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.inject.Inject


@RestController
@ProtectedWithClaims(issuer = OIDCIssuer.EKSTERN)
@RequestMapping(value = ["/api/gcp/arbeidsforhold"])
class ArbeidsforholdControllerGCP @Inject constructor(
    private val oidcContextHolder: TokenValidationContextHolder,
    private val metrikk: Metrikk,
    private val aaregConsumer: AaregConsumer,
    private val brukertilgangService: BrukertilgangService
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getArbeidsforhold(
        @RequestParam("fnr") fnr: String,
        @RequestParam("virksomhetsnummer") virksomhetsnummer: String,
        @RequestParam("fom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fom: LocalDate
    ): ResponseEntity<List<ArbeidsforholdGCP>> {
        val innloggetIdent: String = OIDCUtil.getSubjectEksternMedThrows(oidcContextHolder)
        if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, fnr)) {
            LOG.error("Ikke tilgang til naermeste leder: Bruker spør om noen andre enn seg selv eller egne ansatte")
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build()
        }
        val arbeidsforhold = aaregConsumer.arbeidstakersFnrStillingerForOrgnummer(fnr, fom, virksomhetsnummer)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(arbeidsforhold.map { it.mapToArbeidsforhold() })
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(ArbeidsforholdControllerGCP::class.java)
    }
}
