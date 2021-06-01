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
class AzureAdTokenClientTest {
    @Inject
    private lateinit var restTemplateMedProxy: RestTemplate
    private lateinit var azureAdTokenConsumer: AzureAdTokenClient
    private lateinit var mockRestServiceServer: MockRestServiceServer
    private val TOKEN_URL = "https://url.nav.no"
    private val expires_in:Long = 3600

    @Before
    fun setup() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplateMedProxy).build()
        azureAdTokenConsumer = AzureAdTokenClient(restTemplateMedProxy, TOKEN_URL, "clientId", "clientSecret")
    }

    @After
    fun tearDown() {
        mockRestServiceServer.verify()
    }

    @Test
    fun henterTokenFraAzureHvisTokenMangler() {
        val issuedOn = Instant.now().minusSeconds(3300L)
        val tokenGyldig = "token"
        val responseBody = azureAdResponseAsJsonString(issuedOn, tokenGyldig)
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBody, MediaType.APPLICATION_JSON))
        val token = azureAdTokenConsumer.getAccessToken("test")
        Assertions.assertThat(token).isEqualTo(tokenGyldig)
    }

    @Test
    fun henterTokenFraAzureHvisTokenErUtlopt() {
        val issuedOnUtlopt = Instant.now().minusSeconds(3900L)
        val tokenUtlopt = "token_utlopt"
        val responseBodyUtlopt = azureAdResponseAsJsonString(issuedOnUtlopt, tokenUtlopt)
        val issuedOnGyldig = Instant.now().minusSeconds(3500L)
        val tokenGyldig = "token_gyldig"
        val responseBodyGyldig = azureAdResponseAsJsonString(issuedOnGyldig, tokenGyldig)
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBodyUtlopt, MediaType.APPLICATION_JSON))
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBodyGyldig, MediaType.APPLICATION_JSON))
        azureAdTokenConsumer.getAccessToken("test")
        val token = azureAdTokenConsumer.getAccessToken("test")
        Assertions.assertThat(token).isEqualTo(tokenGyldig)
    }

    @Test
    fun brukerEksisterendeTokenHvisGyldig() {
        val issuedOnGyldig = Instant.now().minusSeconds(300L)
        val tokenGyldig = "token_gyldig"
        val responseBodyGyldig = azureAdResponseAsJsonString(issuedOnGyldig, tokenGyldig)
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBodyGyldig, MediaType.APPLICATION_JSON))
        azureAdTokenConsumer.getAccessToken("test")
        val token = azureAdTokenConsumer.getAccessToken("test")
        Assertions.assertThat(token).isEqualTo(tokenGyldig)
    }

    @Test
    fun henterTokenFraAzureHvisTokenForResourceMangler() {
        val issuedOn1 = Instant.now().minusSeconds(600L)
        val tokenForResource1 = "token_1"
        val resource1 = "resource_1"
        val responseBody1 = azureAdResponseAsJsonString(issuedOn1, tokenForResource1)
        val issuedOn2 = Instant.now().minusSeconds(300L)
        val tokenForResource2 = "token_2"
        val resource2 = "resource_2"
        val responseBody2 = azureAdResponseAsJsonString(issuedOn2, tokenForResource2)
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBody1, MediaType.APPLICATION_JSON))
        mockRestServiceServer.expect(ExpectedCount.once(), MockRestRequestMatchers.requestTo(TOKEN_URL)).andRespond(MockRestResponseCreators.withSuccess(responseBody2, MediaType.APPLICATION_JSON))
        val token1 = azureAdTokenConsumer.getAccessToken(resource1)
        val token2 = azureAdTokenConsumer.getAccessToken(resource2)
        val token3 = azureAdTokenConsumer.getAccessToken(resource2)
        Assertions.assertThat(token1).isEqualTo(tokenForResource1)
        Assertions.assertThat(token2).isEqualTo(tokenForResource2)
        Assertions.assertThat(token3).isEqualTo(tokenForResource2)
    }

    private fun azureAdResponseAsJsonString(issuedOn: Instant, token: String): String {
        val objectMapper = ObjectMapper()
        val module = JavaTimeModule()
        objectMapper.registerModule(module)
        val azureAdResponse = AzureAdResponse()
                .access_token(token)
                .token_type("Bearer")
                .expires_in(expires_in)
                .issuedOn(issuedOn)
        return try {
            objectMapper.writeValueAsString(azureAdResponse)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }
}
