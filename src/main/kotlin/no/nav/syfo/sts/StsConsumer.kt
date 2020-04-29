package no.nav.syfo.sts

import no.nav.syfo.metric.Metrikk
import no.nav.syfo.sts.StsToken
import no.nav.syfo.util.RestUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class StsConsumer @Autowired constructor(
        private val metrikk: Metrikk,
        @Value("\${srv.password}") private val password: String,
        @Qualifier("scheduler") private val restTemplate: RestTemplate,
        @Value("\${security.token.service.rest.url}") private val url: String,
        @Value("\${srv.username}") private val username: String
) {
    private var cachedOidcToken: StsToken? = null
    fun token(): String {
        if (StsToken.shouldRenew(cachedOidcToken)) {
            val request = HttpEntity<Any>(authorizationHeader())
            try {
                val response = restTemplate.exchange(
                        stsTokenUrl,
                        HttpMethod.GET,
                        request,
                        StsToken::class.java
                )
                cachedOidcToken = response.body
                metrikk.tellHendelse(METRIC_CALL_STS_SUCCESS)
            } catch (e: RestClientResponseException) {
                LOG.error("Request to get STS failed with status: ${e.rawStatusCode} and message: ${e.responseBodyAsString}")
                metrikk.tellHendelse(METRIC_CALL_STS_FAIL)
                throw e
            }
        }
        return cachedOidcToken!!.access_token
    }

    private val stsTokenUrl: String
        get() = "$url/rest/v1/sts/token?grant_type=client_credentials&scope=openid"

    private fun authorizationHeader(): HttpHeaders {
        val credentials = RestUtils.basicCredentials(username, password)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, credentials)
        return headers
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(StsConsumer::class.java)

        const val METRIC_CALL_STS_SUCCESS = "call_sts_success"
        const val METRIC_CALL_STS_FAIL = "call_sts_fail"
    }
}
