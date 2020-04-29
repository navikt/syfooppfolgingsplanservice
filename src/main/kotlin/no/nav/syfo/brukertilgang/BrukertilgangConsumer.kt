package no.nav.syfo.brukertilgang

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oidc.OIDCIssuer
import no.nav.syfo.oidc.OIDCUtil
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class BrukertilgangConsumer @Autowired constructor(
        private val oidcContextHolder: OIDCRequestContextHolder,
        private val restTemplate: RestTemplate,
        private val metrikk: Metrikk,
        @Value("\${syfobrukertilgang.url}") private val baseUrl: String
) {
    fun hasAccessToAnsatt(ansattFnr: String): Boolean {
        val httpEntity = entity()
        return try {
            val response = restTemplate.exchange(
                    arbeidstakerUrl(ansattFnr),
                    HttpMethod.GET,
                    httpEntity,
                    Boolean::class.java
            )
            metrikk.countOutgoingReponses(METRIC_CALL_BRUKERTILGANG, response.statusCodeValue)
            response.body
        } catch (e: RestClientResponseException) {
            metrikk.countOutgoingReponses(METRIC_CALL_BRUKERTILGANG, e.rawStatusCode)
            if (e.rawStatusCode == 401) {
                throw RequestUnauthorizedException("Unauthorized request to get access to Ansatt from Syfobrukertilgang")
            } else {
                LOG.error("Error requesting ansatt access from syfobrukertilgang with callId {}: ", httpEntity.headers[NAV_CALL_ID_HEADER], e)
                throw e
            }
        }
    }

    private fun entity(): HttpEntity<*> {
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, RestUtils.bearerHeader(OIDCUtil.getIssuerToken(oidcContextHolder, OIDCIssuer.EKSTERN)))
        headers.add(NAV_CALL_ID_HEADER, createCallId())
        headers.add(NAV_CONSUMER_ID_HEADER, APP_CONSUMER_ID)
        return HttpEntity<Any>(headers)
    }

    private fun arbeidstakerUrl(ansattFnr: String): String {
        return "$baseUrl/api/v1/tilgang/ansatt/$ansattFnr"
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BrukertilgangConsumer::class.java)

        const val METRIC_CALL_BRUKERTILGANG = "call_syfobrukertilgang"
    }
}
