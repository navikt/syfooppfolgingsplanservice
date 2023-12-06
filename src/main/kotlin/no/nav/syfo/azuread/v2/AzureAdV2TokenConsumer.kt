package no.nav.syfo.azuread.v2

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.*
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Component
class AzureAdV2TokenConsumer @Autowired constructor(
    @Qualifier("restTemplateMedProxy") private val restTemplateWithProxy: RestTemplate,
    @Value("\${azure.app.client.id}") private val azureAppClientId: String,
    @Value("\${azure.app.client.secret}") private val azureAppClientSecret: String,
    @Value("\${azure.openid.config.token.endpoint}") private val azureTokenEndpoint: String
) {
    fun getOnBehalfOfToken(
        scopeClientId: String,
        token: String
    ): String {
        return getToken(
            requestEntity = requestEntity(scopeClientId, token)
        ).accessToken
    }

    fun getSystemToken(
        scopeClientId: String
    ): String {
        return getToken(
            requestEntity = systemTokenRequestEntity(scopeClientId)
        ).accessToken
    }

    private fun getToken(
        requestEntity: HttpEntity<MultiValueMap<String, String>>
    ): AzureAdV2Token {
        try {
            val response = restTemplateWithProxy.exchange(
                azureTokenEndpoint,
                HttpMethod.POST,
                requestEntity,
                AzureAdV2TokenResponse::class.java
            )
            val tokenResponse = response.body!!
            return tokenResponse.toAzureAdV2Token()
        } catch (e: RestClientResponseException) {
            val scope = requestEntity.headers["scope"]
            log.error("Call to get AzureADV2Token from AzureAD for scope: $scope with status: ${e.statusCode.value()} and message: ${e.responseBodyAsString}", e)
            throw e
        }
    }

    private fun requestEntity(
        scopeClientId: String,
        token: String
    ): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", azureAppClientId)
        body.add("client_secret", azureAppClientSecret)
        body.add("client_assertion_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
        body.add("assertion", token)
        body.add("scope", "api://$scopeClientId/.default")
        body.add("requested_token_use", "on_behalf_of")
        return HttpEntity(body, headers)
    }

    private fun systemTokenRequestEntity(
        scopeClientId: String
    ): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", azureAppClientId)
        body.add("scope", "api://$scopeClientId/.default")
        body.add("grant_type", "client_credentials")
        body.add("client_secret", azureAppClientSecret)

        return HttpEntity(body, headers)
    }


    companion object {
        private val log = LoggerFactory.getLogger(AzureAdV2TokenConsumer::class.java)
    }
}
