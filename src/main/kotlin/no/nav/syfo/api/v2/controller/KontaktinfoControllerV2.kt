package no.nav.syfo.api.v2.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.v2.domain.Kontaktinfo
import no.nav.syfo.api.v2.util.fodselsnummerInvalid
import no.nav.syfo.dkif.DigitalKontaktinfo
import no.nav.syfo.dkif.DkifConsumer
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows
import no.nav.syfo.service.BrukertilgangService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.inject.Inject


@RestController
@ProtectedWithClaims(issuer = OIDCIssuer.EKSTERN)
@RequestMapping(value = ["/api/v2/kontaktinfo/{fnr}"])
class KontaktinfoControllerV2 @Inject constructor(
    private val oidcContextHolder: TokenValidationContextHolder,
    private val brukertilgangService: BrukertilgangService,
    private val dkifConsumer: DkifConsumer
) {
    @ResponseBody
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getNarmesteLedere(
        @PathVariable("fnr") fnr: String
    ): ResponseEntity<Kontaktinfo> {
        return if (fodselsnummerInvalid(fnr)) {
            LOG.error("Feil i format på fodselsnummer i request til .../v2/kontaktinfo/...")
            ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build()
        } else {
            val innloggetFnr = getSubjectEksternMedThrows(oidcContextHolder)
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetFnr, fnr)) {
                LOG.error("Ikke tilgang til .../v2/kontaktinfo/... : Bruker spør om noen andre enn seg selv eller egne ansatte")
                ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build()
            } else {
                val kontaktinfo = dkifConsumer.kontaktinformasjon(fnr)
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Kontaktinfo(
                        fnr = fnr,
                        epost = kontaktinfo.epostadresse,
                        tlf = kontaktinfo.mobiltelefonnummer,
                        skalHaVarsel = kanVarsles(kontaktinfo)
                    ))
            }
        }

    }

    private fun kanVarsles(digitalKontaktinfo: DigitalKontaktinfo) =
        (digitalKontaktinfo.reservert == false) && digitalKontaktinfo.kanVarsles

    companion object {
        private val LOG = LoggerFactory.getLogger(KontaktinfoControllerV2::class.java)
    }
}
