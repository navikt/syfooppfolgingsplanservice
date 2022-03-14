package no.nav.syfo.api.gcp.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.gcp.domain.PersonGCP
import no.nav.syfo.api.gcp.util.fodselsnummerInvalid
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.pdl.fullName
import no.nav.syfo.service.BrukertilgangService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = OIDCIssuer.EKSTERN)
@RequestMapping(value = ["/api/gcp/person/{fnr}"])
class PersonControllerGCP @Inject constructor(
    private val oidcContextHolder: TokenValidationContextHolder,
    private val pdlConsumer: PdlConsumer,
    private val brukertilgangService: BrukertilgangService)
{
    @ResponseBody
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPerson(
        @PathVariable("fnr") fnr: String
    ): ResponseEntity<PersonGCP> {
        val innloggetFnr = getSubjectEksternMedThrows(oidcContextHolder)
        return if (fodselsnummerInvalid(fnr)) {
            LOG.error("Fant ikke oppslaatt Ident ved henting av narmeste leder for Ident")
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .build()
        } else {
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetFnr, fnr)) {
                LOG.error("Ikke tilgang til person: Bruker spør om noen andre enn seg selv eller egne ansatte")
                ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build()
            } else {
                val personNavn = pdlConsumer.person(fnr)?.fullName()
                personNavn?.let {
                    ResponseEntity
                        .status(HttpStatus.OK)
                        .body(PersonGCP(
                            fnr = fnr,
                            navn = it
                        ))
                } ?: ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build()
            }
        }

    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(PersonControllerGCP::class.java)
    }
}
