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

        const val TILGANG_TIL_BRUKER_VIA_AZURE_V2_PATH = "/navident/bruker"
        const val TILGANG_TIL_SYFO_VIA_AZURE_V2_PATH = "/navident/syfo"
    }
}
