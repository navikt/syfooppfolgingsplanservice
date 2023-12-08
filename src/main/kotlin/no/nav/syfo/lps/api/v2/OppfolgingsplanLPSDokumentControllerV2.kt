package no.nav.syfo.lps.api.v2

import jakarta.ws.rs.ForbiddenException
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.lps.OppfolgingsplanLPSService
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.veiledertilgang.VeilederTilgangConsumer
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = OIDCIssuer.INTERN_AZUREAD_V2)
@RequestMapping(value = ["/api/internad/v2/dokument/lps/{uuid}"])
class OppfolgingsplanLPSDokumentControllerV2 @Inject constructor(
    private val oppfolgingsplanLPSService: OppfolgingsplanLPSService,
    private val veilederTilgangConsumer: VeilederTilgangConsumer
) {
    @GetMapping
    fun pdf(
        @PathVariable("uuid") oppfolgingsplanLPSUUID: UUID
    ): ResponseEntity<*> {
        val planLPS = oppfolgingsplanLPSService.get(oppfolgingsplanLPSUUID)

        veilederTilgangConsumer.throwExceptionIfVeilederWithoutAccessWithOBO(Fodselsnummer(planLPS.fnr))

        if (!planLPS.deltMedNav) {
            throw ForbiddenException()
        } else if (planLPS.pdf == null) {
            throw RuntimeException("No PDF was found for requested Plan")
        } else {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(planLPS.pdf)
        }
    }
}
