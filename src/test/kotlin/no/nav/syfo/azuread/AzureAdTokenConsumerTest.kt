package no.nav.syfo.azuread

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.syfo.LocalApplication
import org.assertj.core.api.Assertions
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import java.time.Instant
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class AzureAdTokenConsumerTest {
    @Inject
    private lateinit var restTemplateMedProxy: RestTemplate
    private lateinit var azureAdTokenConsumer: AzureAdTokenConsumer
    private lateinit var mockRestServiceServer: MockRestServiceServer
    private val TOKEN_URL = "https://url.nav.no"

    @Before
    fun setup() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplateMedProxy).build()
        azureAdTokenConsumer = AzureAdTokenConsumer(restTemplateMedProxy, TOKEN_URL, "clientId", "clientSecret")
    }

    @After
    fun tearDown() {
        mockRestServiceServer.verify()
    }

    @Test
    fun henterTokenFraAzureHvisTokenMangler() {
        val expiresOn = Instant.now().plusSeconds(300L)
        val tokenGyldig = "token"
        val responseBody = azureAdResponseAsJsonString(expiresOn, "ressursID", tokenGyldig)
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBody, MediaType.APPLICATION_JSON))
        val token = azureAdTokenConsumer.getAccessToken("test")
        Assertions.assertThat(token).isEqualTo(tokenGyldig)
    }

    @Test
    fun henterTokenFraAzureHvisTokenErUtlopt() {
        val expiresOnUtlopt = Instant.now().minusSeconds(30L)
        val tokenUtlopt = "token_utlopt"
        val responseBodyUtlopt = azureAdResponseAsJsonString(expiresOnUtlopt, "ressursID", tokenUtlopt)
        val expiresOnGyldig = Instant.now().plusSeconds(300L)
        val tokenGyldig = "token_gyldig"
        val responseBodyGyldig = azureAdResponseAsJsonString(expiresOnGyldig, "ressursID", tokenGyldig)
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBodyUtlopt, MediaType.APPLICATION_JSON))
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBodyGyldig, MediaType.APPLICATION_JSON))
        azureAdTokenConsumer.getAccessToken("test")
        val token = azureAdTokenConsumer.getAccessToken("test")
        Assertions.assertThat(token).isEqualTo(tokenGyldig)
    }

    @Test
    fun brukerEksisterendeTokenHvisGyldig() {
        val expiresOnGyldig = Instant.now().plusSeconds(300L)
        val tokenGyldig = "token_gyldig"
        val responseBodyGyldig = azureAdResponseAsJsonString(expiresOnGyldig, "ressursID", tokenGyldig)
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBodyGyldig, MediaType.APPLICATION_JSON))
        azureAdTokenConsumer.getAccessToken("test")
        val token = azureAdTokenConsumer.getAccessToken("test")
        Assertions.assertThat(token).isEqualTo(tokenGyldig)
    }

    @Test
    fun henterTokenFraAzureHvisTokenForResourceMangler() {
        val expiresOn1 = Instant.now().minusSeconds(300L)
        val tokenForResource1 = "token_1"
        val resource1 = "resource_1"
        val responseBody1 = azureAdResponseAsJsonString(expiresOn1, resource1, tokenForResource1)
        val expiresOn2 = Instant.now().plusSeconds(300L)
        val tokenForResource2 = "token_2"
        val resource2 = "resource_2"
        val responseBody2 = azureAdResponseAsJsonString(expiresOn2, resource2, tokenForResource2)
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBody1, MediaType.APPLICATION_JSON))
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBody2, MediaType.APPLICATION_JSON))
        val token1 = azureAdTokenConsumer.getAccessToken(resource1)
        val token2 = azureAdTokenConsumer.getAccessToken(resource2)
        val token3 = azureAdTokenConsumer.getAccessToken(resource2)
        Assertions.assertThat(token1).isEqualTo(token1)
        Assertions.assertThat(token2).isEqualTo(token2)
        Assertions.assertThat(token3).isEqualTo(token2)
    }

    private fun azureAdResponseAsJsonString(expiresOn: Instant, resource: String, token: String): String {
        val objectMapper = ObjectMapper()
        val module = JavaTimeModule()
        objectMapper.registerModule(module)
        val azureAdResponse = AzureAdResponse()
            .access_token(token)
            .token_type("Bearer")
            .expires_in("3600")
            .ext_expires_in("3600")
            .expires_on(expiresOn)
            .not_before(java.lang.Long.toString(expiresOn.epochSecond))
            .resource(resource)
        return try {
            objectMapper.writeValueAsString(azureAdResponse)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }
}
