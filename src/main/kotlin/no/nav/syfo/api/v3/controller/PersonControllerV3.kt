package no.nav.syfo.api.v3.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.v2.domain.Person
import no.nav.syfo.api.v2.util.fodselsnummerInvalid
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.pdl.fullName
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
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v3/person/{fnr}"])
class PersonControllerV3 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val pdlConsumer: PdlConsumer,
    private val brukertilgangService: BrukertilgangService,
    @Value("\${tokenx.idp}")
    private val tokenxIdp: String,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {
    @ResponseBody
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPerson(
        @PathVariable("fnr") fnr: String
    ): ResponseEntity<Person> {
        val innloggetFnr = TokenXUtil.validateTokenXClaims(contextHolder, tokenxIdp, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        return if (fodselsnummerInvalid(fnr)) {
            LOG.error("Fant ikke oppslaatt Ident ved henting person fra .../v2/person/...")
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .build()
        } else {
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetFnr, fnr)) {
                LOG.error("Ikke tilgang til .../v2/person/... : Bruker spør om noen andre enn seg selv eller egne ansatte")
                ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build()
            } else {
                val personNavn = pdlConsumer.person(fnr)?.fullName()
                personNavn?.let {
                    ResponseEntity
                        .status(HttpStatus.OK)
                        .body(
                            Person(
                                fnr = fnr,
                                navn = it
                            )
                        )
                } ?: ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build()
            }
        }

    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(PersonControllerV3::class.java)
    }
}
