package no.nav.syfo.arkivporten

import no.nav.syfo.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.pdl.PdlConsumer
import no.nav.syfo.util.bearerHeader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class ArkivportenConsumer(
    private val metric: Metrikk,
    @Value("\${arkivporten.url}") private val arkivportenUrl: String,
    @Value("\${arkivporten.scope}") private val arkivportenScope: String,
    private val azureAdV2TokenConsumer: AzureAdV2TokenConsumer,
    @param:Qualifier("scheduler") private val restTemplate: RestTemplate
) : InitializingBean {
    companion object {
        private val LOG = LoggerFactory.getLogger(PdlConsumer::class.java)
        lateinit var arkivportenConsumer: ArkivportenConsumer
        const val ARKIVPORTEN_DOCUMENT_PATH = "/internal/api/v1/documents"
        const val METRIC_CALL_ARKIVPORTEN = "call_arkivporten"
    }

    fun sendDocument(document: Document) {
        try {
            val entity = createRequestEntity(document)
            val response = restTemplate.exchange(
                "$arkivportenUrl$ARKIVPORTEN_DOCUMENT_PATH",
                HttpMethod.POST,
                entity,
                Void::class.java
            )
            metric.countOutgoingReponses(METRIC_CALL_ARKIVPORTEN, response.statusCode.value())
        } catch (e: RestClientResponseException) {
            metric.countOutgoingReponses(METRIC_CALL_ARKIVPORTEN, e.statusCode.value())
            LOG.error("Error sending document to Arkivporten", e)
            throw e
        }
    }

    private fun createRequestEntity(request: Document): HttpEntity<Document> {
        val token: String = azureAdV2TokenConsumer.getSystemToken(arkivportenScope)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set(AUTHORIZATION, bearerHeader(token))
        return HttpEntity(request, headers)
    }

    override fun afterPropertiesSet() {
        arkivportenConsumer = this
    }
}
