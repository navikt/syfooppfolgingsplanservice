package no.nav.syfo.brukertilgang.v2

import jakarta.ws.rs.ForbiddenException
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.brukertilgang.BrukerTilgang
import no.nav.syfo.brukertilgang.BrukertilgangConsumer
import no.nav.syfo.brukertilgang.RSTilgang
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.service.BrukertilgangService
import no.nav.syfo.tokenx.TokenXUtil
import no.nav.syfo.tokenx.TokenXUtil.TokenXIssuer.TOKENX
import no.nav.syfo.tokenx.TokenXUtil.fnrFromIdportenTokenX
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.MultiValueMap
import org.springframework.util.ObjectUtils
import org.springframework.web.bind.annotation.*
import javax.inject.Inject

@RestController
@ProtectedWithClaims(issuer = TOKENX, claimMap = ["acr=Level4", "acr=idporten-loa-high"], combineWithOr = true)
@RequestMapping(value = ["/api/v2/tilgang"])
class BrukerTilgangControllerV2 @Inject constructor(
    private val contextHolder: TokenValidationContextHolder,
    private val brukertilgangConsumer: BrukertilgangConsumer,
    private val brukertilgangService: BrukertilgangService,
    private val metrikk: Metrikk,
    @Value("\${oppfolgingsplan.frontend.client.id}")
    private val oppfolgingsplanClientId: String,
) {
    @GetMapping
    fun harTilgang(@RequestParam(value = "fnr", required = false) oppslaattFnr: String?): RSTilgang {
        val innloggetIdent = TokenXUtil.validateTokenXClaims(contextHolder, oppfolgingsplanClientId)
            .fnrFromIdportenTokenX()
            .value
        val oppslaattIdent = if (ObjectUtils.isEmpty(oppslaattFnr)) innloggetIdent else oppslaattFnr
        if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, oppslaattIdent)) {
            LOG.error("Ikke tilgang: Bruker spør om noen andre enn seg selv eller egne ansatte")
            throw ForbiddenException()
        }
        metrikk.tellHendelse("sjekk_brukertilgang")
        return RSTilgang(true)
    }

    @GetMapping(path = ["/ansatt"])
    @ResponseBody
    fun accessToAnsatt(@RequestHeader headers: MultiValueMap<String, String>): BrukerTilgang {
        val oppslaattIdent = headers.getFirst(NAV_PERSONIDENT_HEADER.lowercase())
        return if (ObjectUtils.isEmpty(oppslaattIdent)) {
            throw IllegalArgumentException("Fant ikke Ident i Header ved sjekk av tilgang til Ident")
        } else {
            metrikk.tellHendelse("accessToIdent")
            BrukerTilgang(brukertilgangConsumer.hasAccessToAnsatt(oppslaattIdent!!))
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BrukerTilgangControllerV2::class.java)
    }
}
