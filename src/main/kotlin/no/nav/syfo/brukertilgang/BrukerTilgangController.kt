package no.nav.syfo.brukertilgang

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.oidc.OIDCUtil
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.pdl.isKode6Or7
import no.nav.syfo.service.BrukertilgangService
import no.nav.syfo.util.HeaderUtil
import org.slf4j.LoggerFactory
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import javax.inject.Inject
import javax.ws.rs.ForbiddenException

@RestController
@ProtectedWithClaims(issuer = OIDCIssuer.EKSTERN)
@RequestMapping(value = ["/api/tilgang"])
class BrukerTilgangController @Inject constructor(
        private val contextHolder: OIDCRequestContextHolder,
        private val brukertilgangConsumer: BrukertilgangConsumer,
        private val brukertilgangService: BrukertilgangService,
        private val pdlConsumer: PdlConsumer,
        private val metrikk: Metrikk
) {
    @GetMapping
    fun harTilgang(@RequestParam(value = "fnr", required = false) oppslaattFnr: String?): RSTilgang {
        val innloggetIdent = OIDCUtil.getSubjectEksternMedThrows(contextHolder)
        val oppslaattIdent = if (StringUtils.isEmpty(oppslaattFnr)) innloggetIdent else oppslaattFnr
        if (!brukertilgangService.tilgangTilOppslattIdent(innloggetIdent, oppslaattIdent)) {
            LOG.error("Ikke tilgang: Bruker spør om noen andre enn seg selv eller egne ansatte")
            throw ForbiddenException()
        }
        metrikk.tellHendelse("sjekk_brukertilgang")
        val isKode6Or7 = pdlConsumer.person(oppslaattIdent!!)?.isKode6Or7()
        return if (isKode6Or7 == false) {
            RSTilgang(true)
        } else {
            RSTilgang(
                    false,
                    IKKE_TILGANG_GRUNN_DISKRESJONSMERKET
            )
        }
    }

    @GetMapping(path = ["/ansatt"])
    @ResponseBody
    fun accessToAnsatt(@RequestHeader headers: MultiValueMap<String, String>): BrukerTilgang {
        val oppslaattIdent = headers.getFirst(HeaderUtil.NAV_PERSONIDENT.toLowerCase())
        return if (StringUtils.isEmpty(oppslaattIdent)) {
            throw IllegalArgumentException("Fant ikke Ident i Header ved sjekk av tilgang til Ident")
        } else {
            metrikk.tellHendelse("accessToIdent")
            BrukerTilgang(brukertilgangConsumer.hasAccessToAnsatt(oppslaattIdent))
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BrukerTilgangController::class.java)
        const val IKKE_TILGANG_GRUNN_DISKRESJONSMERKET = "kode6-7"
    }

}
