package no.nav.syfo.brukertilgang

import no.nav.syfo.metric.Metrikk
import no.nav.syfo.tokenx.tokendings.TokenDingsConsumer
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
    private val restTemplate: RestTemplate,
    private val metrikk: Metrikk,
    private val tokenDingsConsumer: TokenDingsConsumer,
    @Value("\${syfobrukertilgang.url}") private val baseUrl: String,
    @Value("\${syfobrukertilgang.id}") private var targetApp: String,
) {
    fun hasAccessToAnsatt(ansattFnr: String, token: String): Boolean {
        val exchangedToken: String = tokenDingsConsumer.exchangeToken(token, targetApp)
        val httpEntity = entity(exchangedToken)
        return try {
            val response = restTemplate.exchange(
                    arbeidstakerUrl(ansattFnr),
                    HttpMethod.GET,
                    httpEntity,
                    Boolean::class.java
            )
            metrikk.countOutgoingReponses(METRIC_CALL_BRUKERTILGANG, response.statusCodeValue)
            response.body!!
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

    private fun entity(exchangedToken: String): HttpEntity<*> {
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(exchangedToken))
        headers.add(NAV_CALL_ID_HEADER, createCallId())
        headers.add(NAV_CONSUMER_ID_HEADER, APP_CONSUMER_ID)
        return HttpEntity<Any>(headers)
    }

    private fun arbeidstakerUrl(ansattFnr: String): String {
        return "$baseUrl/api/v2/tilgang/ansatt/$ansattFnr"
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BrukertilgangConsumer::class.java)

        const val METRIC_CALL_BRUKERTILGANG = "call_syfobrukertilgang"
    }
}
