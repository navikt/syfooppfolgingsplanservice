package no.nav.syfo.lps

import no.nav.syfo.metric.Metrikk
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.*
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import javax.inject.Inject

@Repository
class OPPdfGenConsumer @Inject constructor(
    @Qualifier("scheduler") private val restTemplate: RestTemplate,
    private val metric: Metrikk
) {
    fun pdfgenResponse(fagmelding: Fagmelding): ByteArray {
        try {
            val response = restTemplate.exchange(
                opPdfGenUrl,
                HttpMethod.POST,
                entity(fagmelding),
                ByteArray::class.java
            )
            val pdf = response.body!!
            metric.tellHendelse(METRIC_CALL_OPPDFGEN_SUCCESS)
            return pdf
        } catch (e: RestClientResponseException) {
            metric.tellHendelse(METRIC_CALL_OPPDFGEN_FAIL)
            val message = "Call to get generate pdf for LPS-plan failed with status: ${e.rawStatusCode} and message: ${e.responseBodyAsString}"
            LOG.error(message)
            throw e
        }
    }

    private fun entity(fagmelding: Fagmelding): HttpEntity<Fagmelding> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers[NAV_CONSUMER_ID_HEADER] = APP_CONSUMER_ID
        headers[NAV_CALL_ID_HEADER] = createCallId()
        return HttpEntity(fagmelding, headers)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OPPdfGenConsumer::class.java)

        private const val opPdfGenUrl = "http://syfooppdfgen/api/v1/genpdf/opservice/oppfolgingsplanlps"

        private const val METRIC_CALL_OPPDFGEN_SUCCESS = "call_oppdfgen_success"
        private const val METRIC_CALL_OPPDFGEN_FAIL = "call_oppdfgen_fail"
    }
}
