package no.nav.syfo.dkif

import com.fasterxml.jackson.databind.ObjectMapper
import javax.inject.Inject
import no.nav.syfo.LocalApplication
import no.nav.syfo.azuread.AzureAdTokenConsumer
import no.nav.syfo.metric.Metrikk
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class DkifConsumerTest {
    val objectMapper = ObjectMapper()
    val fnr = "12345678901"

    @Inject
    private lateinit var restTemplate: RestTemplate

    @MockBean
    private lateinit var azureAdTokenConsumer: AzureAdTokenConsumer

    @Value("\${dkif.scope}")
    private lateinit var dkifScope: String

    @Value("\${dkif.url}")
    private lateinit var url: String
    private lateinit var dkifConsumer: DkifConsumer

    @Inject
    private lateinit var metrikk: Metrikk
    private lateinit var mockRestServiceServer: MockRestServiceServer

    @Before
    fun setUp() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        Mockito.`when`(azureAdTokenConsumer.getAccessToken(eq(dkifScope))).thenReturn("token")
        dkifConsumer = DkifConsumer(restTemplate, azureAdTokenConsumer, metrikk, dkifScope, url)
    }

    @After
    fun tearDown() {
        mockRestServiceServer.verify()
    }

    @Test(expected = RestClientException::class)
    @Throws(RestClientException::class)
    fun `GET kontaktinformasjon throws RestClientResponseException if received response does not deserialize correctly`() {
        mockRestServiceServer.expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo(url),
        ).andExpect { it.method == HttpMethod.POST }
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                    .body(
                        """
                        "not":
                          "expected": true
                    """.trimIndent()
                    )
            )
        dkifConsumer.kontaktinformasjon(fnr)
    }

    @Test(expected = RestClientResponseException::class)
    @Throws(RestClientResponseException::class)
    fun `GET kontaktinformasjon throws DKIFRequestFailedException if we did not get response without body`() {
        val personerResponse = PostPersonerResponse(
            personer = mapOf(fnr to DigitalKontaktinfo(true, true, null, null)),
        )
        mockRestServiceServer.expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo(url),
        ).andExpect { it.method == HttpMethod.POST }
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
            )
        dkifConsumer.kontaktinformasjon(fnr)
    }

    @Test(expected = DKIFRequestFailedException::class)
    @Throws(DKIFRequestFailedException::class)
    fun `GET kontaktinformasjon throws DKIFRequestFailedException if we get 2xx but requested fnr is missing from response`() {
        val personerResponse = PostPersonerResponse(
            personer = emptyMap(),
        )
        mockRestServiceServer.expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo(url),
        ).andExpect { it.method == HttpMethod.POST }
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                    .body(
                        objectMapper.writeValueAsString(personerResponse)
                    )
            )
        dkifConsumer.kontaktinformasjon(fnr)
    }

    @Test
    fun `GET kontaktinformasjon returns Kontaktinfo when we get response with requested person`() {
        val digitalKontaktinfo = DigitalKontaktinfo(kanVarsles = true, reservert = false, null, null)
        val personerResponse = PostPersonerResponse(
            personer = mapOf(fnr to digitalKontaktinfo),
        )
        mockRestServiceServer.expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo(url),
        ).andExpect { it.method == HttpMethod.POST }
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                    .body(
                        objectMapper.writeValueAsString(personerResponse)
                    )
            )
        val kontaktinformasjon = dkifConsumer.kontaktinformasjon(fnr)
        assertThat(kontaktinformasjon.kanVarsles).isEqualTo(digitalKontaktinfo.kanVarsles)
        assertThat(kontaktinformasjon.reservert).isEqualTo(digitalKontaktinfo.reservert)
    }
}