package no.nav.syfo.veiledertilgang

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.oidc.OIDCUtil
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.ws.rs.ForbiddenException

@Service
class VeilederTilgangConsumer(
    @Value("\${syfotilgangskontroll.client.id}") private val syfotilgangskontrollClientId: String,
    @Value("\${tilgangskontrollapi.url}") private val tilgangskontrollUrl: String,
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer,
    private val metric: Metrikk,
    private val contextHolder: TokenValidationContextHolder,
    private val template: RestTemplate
) {
    private val accessToPersonUriTemplate: UriComponentsBuilder
    private val accessToSYFOUriTemplate: UriComponentsBuilder

    fun throwExceptionIfVeilederWithoutAccessWithOBO(fnr: Fodselsnummer) {
        val harTilgang = hasVeilederAccessToPersonWithOBO(fnr)
        if (!harTilgang) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToPersonWithOBO(fnr: Fodselsnummer): Boolean {
        val oboToken = azureAdV2TokenConsumer.getOnBehalfOfToken(
            scopeClientId = syfotilgangskontrollClientId,
            token = OIDCUtil.getIssuerToken(contextHolder, OIDCIssuer.INTERN_AZUREAD_V2)
        )
        val url = "$tilgangskontrollUrl$TILGANG_TIL_BRUKER_VIA_AZURE_V2_PATH/${fnr.value}"
        return checkAccess(
            token = oboToken,
            url = url
        )
    }

    fun throwExceptionIfVeilederWithoutAccessToSYFOWithOBO() {
        val harTilgang = hasVeilederAccessToSYFOWithOBO()
        if (!harTilgang) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToSYFOWithOBO(): Boolean {
        val oboToken = azureAdV2TokenConsumer.getOnBehalfOfToken(
            scopeClientId = syfotilgangskontrollClientId,
            token = OIDCUtil.getIssuerToken(contextHolder, OIDCIssuer.INTERN_AZUREAD_V2)
        )
        val url = "$tilgangskontrollUrl$TILGANG_TIL_SYFO_VIA_AZURE_V2_PATH"
        return checkAccess(
            token = oboToken,
            url = url
        )
    }

    fun throwExceptionIfVeilederWithoutAccess(fnr: Fodselsnummer) {
        val harTilgang = hasVeilederAccessToPerson(fnr)
        if (!harTilgang) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToPerson(fnr: Fodselsnummer): Boolean {
        val tilgangTilBrukerViaAzureUriMedFnr = accessToPersonUriTemplate.build(Collections.singletonMap(FNR, fnr.value))
        return checkAccess(
            token = OIDCUtil.getIssuerToken(contextHolder, OIDCIssuer.AZURE),
            url = tilgangTilBrukerViaAzureUriMedFnr.toURL().toString()
        )
    }

    fun throwExceptionIfVeilederWithoutAccessToSYFO() {
        val harTilgang = hasVeilederAccessToSYFO()
        if (!harTilgang) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToSYFO(): Boolean {
        val tilgangTilTjenesteUri = accessToSYFOUriTemplate.build().toUri()
        return checkAccess(
            token = OIDCUtil.getIssuerToken(contextHolder, OIDCIssuer.AZURE),
            url = tilgangTilTjenesteUri.toURL().toString()
        )
    }

    private fun checkAccess(
        token: String,
        url: String
    ): Boolean {
        val httpEntity = entity(token = token)
        return try {
            template.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                String::class.java
            )
            true
        } catch (e: HttpClientErrorException) {
            if (e.rawStatusCode == 403) {
                false
            } else {
                metric.tellHendelse(METRIC_CALL_VEILEDERTILGANG_USER_FAIL)
                LOG.error("Error requesting ansatt access from syfobrukertilgang with status-${e.rawStatusCode} callId-${httpEntity.headers[NAV_CALL_ID_HEADER]}: ", e)
                throw e
            }
        }
    }

    private fun entity(token: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.setBearerAuth(token)
        headers[NAV_CALL_ID_HEADER] = createCallId()
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        return HttpEntity(headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(VeilederTilgangConsumer::class.java)

        private const val METRIC_CALL_VEILEDERTILGANG_BASE = "call_syfotilgangskontroll"
        private const val METRIC_CALL_VEILEDERTILGANG_USER_FAIL = "${METRIC_CALL_VEILEDERTILGANG_BASE}_user_fail"

        const val FNR = "fnr"
        const val TILGANG_TIL_BRUKER_VIA_AZURE_PATH = "/bruker"
        const val TILGANG_TIL_TJENESTEN_VIA_AZURE_PATH = "/syfo"
        private const val FNR_PLACEHOLDER = "{$FNR}"

        const val TILGANG_TIL_BRUKER_VIA_AZURE_V2_PATH = "/navident/bruker"
        const val TILGANG_TIL_SYFO_VIA_AZURE_V2_PATH = "/navident/syfo"
    }

    init {
        accessToPersonUriTemplate = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
            .path(TILGANG_TIL_BRUKER_VIA_AZURE_PATH)
            .queryParam(FNR, FNR_PLACEHOLDER)
        accessToSYFOUriTemplate = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
            .path(TILGANG_TIL_TJENESTEN_VIA_AZURE_PATH)
    }
}
