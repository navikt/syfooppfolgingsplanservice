package no.nav.syfo.api.gcp.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.gcp.domain.NarmesteLederGCP
import no.nav.syfo.api.gcp.domain.mapToNarmesteLederGCP
import no.nav.syfo.api.gcp.util.fodselsnummerInvalid
import no.nav.syfo.api.selvbetjening.controller.NarmesteLederController
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.narmesteleder.NarmesteLederConsumer
import no.nav.syfo.oidc.OIDCIssuer.EKSTERN
import no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows
import no.nav.syfo.service.BrukertilgangService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = ["/api/gcp/narmesteleder/{fnr}"])
class NarmesteLederControllerGCP @Inject constructor(
    private val oidcContextHolder: TokenValidationContextHolder,
    private val metrikk: Metrikk,
    private val brukertilgangService: BrukertilgangService,
    private val narmesteLederConsumer: NarmesteLederConsumer
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getNarmesteLeder(
        @PathVariable("fnr") fnr: String,
        @RequestParam("virksomhetsnummer") virksomhetsnummer: String
    ): ResponseEntity<NarmesteLederGCP> {
        metrikk.tellHendelse("get_narmesteleder")
        return if (fodselsnummerInvalid(fnr)) {
            LOG.error("Fant ikke oppslaatt Ident ved henting av narmeste leder for Ident")
            throw IllegalArgumentException("Fant ikke Ident i Header ved henting av naermeste leder for ident")
        } else {
            val innloggetIdent: String = getSubjectEksternMedThrows(oidcContextHolder)
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, fnr)) {
                LOG.error("Ikke tilgang til naermeste leder: Bruker spør om noen andre enn seg selv eller egne ansatte")
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build()
            }
            val narmesteLeder = narmesteLederConsumer.narmesteLeder(fnr, virksomhetsnummer)
            if (narmesteLeder.isPresent) {
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(narmesteLeder.get().mapToNarmesteLederGCP())
            } else {
                metrikk.tellHendelse("get_narmesteleder_no_content")
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build()
            }
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(NarmesteLederController::class.java)
    }
}