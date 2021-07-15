package no.nav.syfo.lps.api.v2

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.lps.OppfolgingsplanLPSService
import no.nav.syfo.lps.api.domain.RSOppfolgingsplanLPS
import no.nav.syfo.lps.mapToRSOppfolgingsplanLPS
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import no.nav.syfo.veiledertilgang.VeilederTilgangConsumer
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = OIDCIssuer.INTERN_AZUREAD_V2)
@RequestMapping(value = ["/api/internad/v2/oppfolgingsplan/lps"])
class OppfolgingsplanLPSControllerV2 @Inject constructor(
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService,
    private val veilederTilgangConsumer: VeilederTilgangConsumer
) {
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    fun oppfolgingsplanlpsList(
        @RequestHeader headers: MultiValueMap<String, String>
    ): List<RSOppfolgingsplanLPS> {
        val personIdent = headers.getFirst(NAV_PERSONIDENT_HEADER.toLowerCase())?: throw IllegalArgumentException("No PersonIdent supplied")
        val personFnr = Fodselsnummer(personIdent)

        veilederTilgangConsumer.throwExceptionIfVeilederWithoutAccessWithOBO(personFnr)

        return oppfolgingsplanLPSService.getSharedWithNAV(personFnr)
            .map {
                it.mapToRSOppfolgingsplanLPS()
            }
    }
}
