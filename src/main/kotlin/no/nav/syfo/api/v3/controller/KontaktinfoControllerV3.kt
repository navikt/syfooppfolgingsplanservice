package no.nav.syfo.api.v3.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.api.util.fodselsnummerInvalid
import no.nav.syfo.api.v3.domain.Kontaktinfo
import no.nav.syfo.dkif.DigitalKontaktinfo
import no.nav.syfo.dkif.DkifConsumer
import no.nav.syfo.service.BrukertilgangService
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4"])
@RequestMapping(value = ["/api/v3/kontaktinfo/{fnr}"])
class KontaktinfoControllerV3 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val brukertilgangService: BrukertilgangService,
    private val dkifConsumer: DkifConsumer,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {
    @ResponseBody
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    fun getKontaktinfo(
        @PathVariable("fnr") fnr: String,
    ): ResponseEntity<Kontaktinfo> {
        val innloggetFnr = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value

        return if (fodselsnummerInvalid(fnr)) {
            LOG.error("Ugyldig fnr ved henting av kontaktinfo")
            ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build()
        } else {
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetFnr, fnr)) {
                LOG.error("Ikke tilgang til kontaktinfo: Bruker spør om noen andre enn seg selv eller egne ansatte")
                ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build()
            } else {
                val kontaktinfo = dkifConsumer.kontaktinformasjon(fnr)
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                        Kontaktinfo(
                            fnr = fnr,
                            epost = kontaktinfo.epostadresse,
                            tlf = kontaktinfo.mobiltelefonnummer,
                            skalHaVarsel = kanVarsles(kontaktinfo),
                        ),
                    )
            }
        }
    }

    private fun kanVarsles(digitalKontaktinfo: DigitalKontaktinfo) =
        (digitalKontaktinfo.reservert == false) && digitalKontaktinfo.kanVarsles

    companion object {
        private val LOG = LoggerFactory.getLogger(KontaktinfoControllerV3::class.java)
    }
}
