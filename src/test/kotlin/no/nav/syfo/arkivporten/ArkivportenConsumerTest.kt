package no.nav.syfo.arkivporten

import java.util.*
import javax.inject.Inject
import no.nav.syfo.LocalApplication
import no.nav.syfo.azuread.v2.AzureAdV2TokenConsumer
import no.nav.syfo.metric.Metrikk
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
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
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class ArkivportenConsumerTest {
    @Inject
    private lateinit var restTemplate: RestTemplate

    @MockBean
    private lateinit var azureAdV2TokenConsumer: AzureAdV2TokenConsumer

    @Value("\${arkivporten.scope}")
    private lateinit var arkivportenScope: String

    @Value("\${arkivporten.url}")
    private lateinit var url: String

    @MockBean
    private lateinit var arkivportenConsumer: ArkivportenConsumer

    @Inject
    private lateinit var metrikk: Metrikk
    private lateinit var mockRestServiceServer: MockRestServiceServer

    @Before
    fun setUp() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        arkivportenConsumer = ArkivportenConsumer(
            metric = metrikk,
            restTemplate = restTemplate,
            azureAdV2TokenConsumer = azureAdV2TokenConsumer,
            arkivportenScope = arkivportenScope,
            arkivportenUrl = url
        )
    }

    @Test
    fun `sendDocument throws RestClientResponseException if outgoing call does not respond with 2xx`() {
        mockRestServiceServer.expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo("$url${ArkivportenConsumer.ARKIVPORTEN_DOCUMENT_PATH}"),
        ).andExpect { it.method == HttpMethod.POST }
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST)
            )
        val document = Document(
            documentId = UUID.randomUUID(),
            type = DocumentType.OPPFOLGINGSPLAN,
            dialogTitle = "Test Document",
            dialogSummary = "Test Dialog Summary",
            content = byteArrayOf(),
            contentType = MediaType.APPLICATION_PDF.toString(),
            orgnumber = "123456789"
        )
        assertThrows<RestClientResponseException> {
            arkivportenConsumer.sendDocument(document)
        }
    }

    @Test
    fun `sendDocument does not throw when outgoing call responds with 200`() {
        mockRestServiceServer.expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo("$url${ArkivportenConsumer.ARKIVPORTEN_DOCUMENT_PATH}"),
        ).andExpect { it.method == HttpMethod.POST }
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
            )
        val document = Document(
            documentId = UUID.randomUUID(),
            type = DocumentType.OPPFOLGINGSPLAN,
            dialogTitle = "Test Document",
            dialogSummary = "Test Dialog Summary",
            content = byteArrayOf(),
            contentType = MediaType.APPLICATION_PDF.toString(),
            orgnumber = "123456789"
        )
        assertDoesNotThrow {
            arkivportenConsumer.sendDocument(document)
        }
    }

}
