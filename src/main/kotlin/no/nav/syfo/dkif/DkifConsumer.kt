package no.nav.syfo.dkif

import no.nav.syfo.azuread.AzureAdTokenConsumer
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.util.*



class DkifConsumer @Autowired constructor (
    private val restTemplate: RestTemplate,
    private val azureAdTokenConsumer: AzureAdTokenConsumer,
    @Value("\${dkif.scope}") private val dkifScope: String,
    @Value("\${dkif.url") val dkifUrl: String
) {

    fun kontaktinformasjon(fnr: String): DigitalKontaktinfo {
        val accessToken = "Bearer ${azureAdTokenConsumer.getAccessToken(dkifScope)}"

        try {
            val response = restTemplate.exchange(
                dkifUrl,
                HttpMethod.GET,
                entity(fnr, accessToken),
                String::class.java
            )
            if (response.statusCode == HttpStatus.OK) {
                return response.body?.let {
                    KontaktinfoMapper.mapPerson(it)
                } ?: throw DKIFRequestFailedException("ReponseBody is null")
            }
            throw DKIFRequestFailedException("Received response with status code: ${response.statusCodeValue}")
        } catch (e: RestClientResponseException) {
            log.error("Error in call to DKIF: ${e.message}", e)
            throw e
        }
    }

    private fun entity(fnr: String, accessToken: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers[HttpHeaders.AUTHORIZATION] = accessToken
        headers[NAV_PERSONIDENT_HEADER] = fnr
        headers[NAV_CALL_ID_HEADER] = createCallId()
        return HttpEntity(headers)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DkifConsumer::class.java.name)

        private fun createCallId(): String {
            val randomUUID = UUID.randomUUID().toString()
            return "syfooppfolgingsplanservice-$randomUUID"
        }
    }
}
