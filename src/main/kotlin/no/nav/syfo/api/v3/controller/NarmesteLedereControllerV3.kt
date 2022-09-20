package no.nav.syfo.api.v3.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.v2.domain.NarmesteLeder
import no.nav.syfo.api.v2.domain.mapToNarmesteLeder
import no.nav.syfo.api.v2.util.fodselsnummerInvalid
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.narmesteleder.NarmesteLedereConsumer
import no.nav.syfo.service.BrukertilgangService
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v3/narmesteledere/{fnr}"])
class NarmesteLedereControllerV3 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val metrikk: Metrikk,
    private val brukertilgangService: BrukertilgangService,
    private val narmesteLedereConsumer: NarmesteLedereConsumer,
    @Value("\${tokenx.idp}")
    private val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {
    @ResponseBody
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getNarmesteLedere(
        @PathVariable("fnr") fnr: String
    ): ResponseEntity<List<NarmesteLeder>> {
        metrikk.tellHendelse("get_narmesteledere")

        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value

        return if (fodselsnummerInvalid(fnr)) {
            LOG.error("Feil i format på fodselsnummer i request til .../v2/narmesteledere/...")
            ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build()
        } else {
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, fnr)) {
                LOG.error("Ikke tilgang til .../v2/narmesteledere/... : Bruker spør om noen andre enn seg selv eller egne ansatte")
                ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build()
            } else {
                val narmesteLedere = narmesteLedereConsumer.narmesteLedere(fnr)
                if (narmesteLedere.isPresent) {
                    ResponseEntity
                        .status(HttpStatus.OK)
                        .body(narmesteLedere.get().map { it.mapToNarmesteLeder() })
                } else {
                    ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .build()
                }
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NarmesteLedereControllerV3::class.java)
    }
}
