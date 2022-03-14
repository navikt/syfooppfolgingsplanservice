package no.nav.syfo.api.selvbetjening.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.aktorregister.AktorregisterConsumer
import no.nav.syfo.api.gcp.domain.NarmesteLederGCP
import no.nav.syfo.api.gcp.domain.mapToNarmesteLederGCP
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.narmesteleder.NarmesteLedereConsumer
import no.nav.syfo.oidc.OIDCIssuer.EKSTERN
import no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows
import no.nav.syfo.service.BrukertilgangService
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = ["/api/gcp/narmesteledere"])
class NarmesteLedereControllerGCP @Inject constructor(
    private val oidcContextHolder: TokenValidationContextHolder,
    private val metrikk: Metrikk,
    private val aktorregisterConsumer: AktorregisterConsumer,
    private val brukertilgangService: BrukertilgangService,
    private val narmesteLedereConsumer: NarmesteLedereConsumer
) {
    @ResponseBody
    @GetMapping
    fun getNarmesteLedere(
        @RequestHeader headers: MultiValueMap<String?, String?>
    ): ResponseEntity<List<NarmesteLederGCP>> {
        metrikk.tellHendelse("get_narmesteledere")
        val oppslaattIdent = headers.getFirst(NAV_PERSONIDENT_HEADER.toLowerCase())
        return if (StringUtils.isEmpty(oppslaattIdent)) {
            LOG.error("Fant ikke oppslaatt Ident ved henting av narmeste ledere for Ident")
            throw IllegalArgumentException("Fant ikke Ident i Header ved henting av naermeste ledere for ident")
        } else {
            val innloggetIdent = getSubjectEksternMedThrows(oidcContextHolder)
            if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, oppslaattIdent)) {
                LOG.error("Ikke tilgang til naermeste ledere: Bruker spør om noen andre enn seg selv eller egne ansatte")
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build()
            }
            val narmesteLeder = narmesteLedereConsumer.narmesteLedere(oppslaattIdent)
            if (narmesteLeder.isPresent) {
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(narmesteLeder.get().map { it.mapToNarmesteLederGCP()})
            } else {
                metrikk.tellHendelse("get_narmesteledere_no_content")
                ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build()
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NarmesteLedereControllerGCP::class.java)
    }
}