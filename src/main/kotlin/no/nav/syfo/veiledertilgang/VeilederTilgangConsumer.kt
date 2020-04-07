package no.nav.syfo.veiledertilgang

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.oidc.OIDCUtil
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.*
import javax.ws.rs.ForbiddenException

@Service
class VeilederTilgangConsumer(
        @Value("\${tilgangskontrollapi.url}") tilgangskontrollUrl: String,
        private val metric: Metrikk,
        private val oidcContextHolder: OIDCRequestContextHolder,
        private val template: RestTemplate
) {
    private val accessToPersonUriTemplate: UriComponentsBuilder
    private val accessToSYFOUriTemplate: UriComponentsBuilder

    fun throwExceptionIfVeilederWithoutAccess(fnr: Fodselsnummer) {
        val harTilgang = hasVeilederAccessToPerson(fnr)
        if (!harTilgang) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToPerson(fnr: Fodselsnummer): Boolean {
        val tilgangTilBrukerViaAzureUriMedFnr = accessToPersonUriTemplate.build(Collections.singletonMap(FNR, fnr.value))
        return checkAccess(tilgangTilBrukerViaAzureUriMedFnr)
    }

    fun throwExceptionIfVeilederWithoutAccessToSYFO() {
        val harTilgang = hasVeilederAccessToSYFO()
        if (!harTilgang) {
            throw ForbiddenException()
        }
    }

    fun hasVeilederAccessToSYFO(): Boolean {
        val tilgangTilTjenesteUri = accessToSYFOUriTemplate.build().toUri()
        return checkAccess(tilgangTilTjenesteUri)
    }

    private fun checkAccess(uri: URI): Boolean {
        val httpEntity = entity()
        return try {
            template.exchange(
                    uri,
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

    private fun entity(): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers[HttpHeaders.AUTHORIZATION] = RestUtils.bearerHeader(OIDCUtil.getIssuerToken(oidcContextHolder, OIDCIssuer.AZURE))
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
    }

    init {
        accessToPersonUriTemplate = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
                .path(TILGANG_TIL_BRUKER_VIA_AZURE_PATH)
                .queryParam(FNR, FNR_PLACEHOLDER)
        accessToSYFOUriTemplate = UriComponentsBuilder.fromHttpUrl(tilgangskontrollUrl)
                .path(TILGANG_TIL_TJENESTEN_VIA_AZURE_PATH)
    }
}