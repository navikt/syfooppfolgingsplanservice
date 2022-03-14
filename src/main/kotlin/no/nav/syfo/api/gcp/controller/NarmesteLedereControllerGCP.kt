package no.nav.syfo.api.selvbetjening.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.api.gcp.domain.NarmesteLederGCP
import no.nav.syfo.api.gcp.domain.mapToNarmesteLederGCP
import no.nav.syfo.api.gcp.util.fodselsnummerInvalid
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.narmesteleder.NarmesteLedereConsumer
import no.nav.syfo.oidc.OIDCIssuer.EKSTERN
import no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows
import no.nav.syfo.service.BrukertilgangService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = ["/api/gcp/narmesteledere/{fnr}"])
class NarmesteLedereControllerGCP @Inject constructor(
    private val oidcContextHolder: TokenValidationContextHolder,
    private val metrikk: Metrikk,
    private val aktorregisterConsumer: AktorregisterConsumer,
    private val brukertilgangService: BrukertilgangService,
    private val narmesteLedereConsumer: NarmesteLedereConsumer
) {
    @ResponseBody
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getNarmesteLedere(
        @PathVariable("fnr") fnr: String
    ): ResponseEntity<List<NarmesteLederGCP>> {
        metrikk.tellHendelse("get_narmesteledere")

        return if (fodselsnummerInvalid(fnr)) {
            LOG.error("Feil i format på fodselsnummer i request til .../narmesteledere/...")
            ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build()
        } else {
            val innloggetIdent = getSubjectEksternMedThrows(oidcContextHolder)
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, fnr)) {
                LOG.error("Ikke tilgang til naermeste ledere: Bruker spør om noen andre enn seg selv eller egne ansatte")
                ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build()
            } else {
                val narmesteLedere = narmesteLedereConsumer.narmesteLedere(fnr)
                if (narmesteLedere.isPresent) {
                    ResponseEntity
                        .status(HttpStatus.OK)
                        .body(narmesteLedere.get().map { it.mapToNarmesteLederGCP() })
                } else {
                    ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .build()
                }
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NarmesteLedereControllerGCP::class.java)
    }
}