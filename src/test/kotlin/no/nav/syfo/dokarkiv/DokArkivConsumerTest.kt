package no.nav.syfo.dokarkiv

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.syfo.LocalApplication
import no.nav.syfo.domain.*
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.sts.StsConsumer
import org.assertj.core.api.Assertions
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.*
import javax.inject.Inject
import org.springframework.http.HttpStatus

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class DokArkivConsumerTest {
    @Inject
    private lateinit var restTemplate: RestTemplate

    @MockBean
    private lateinit var stsConsumer: StsConsumer

    @Value("\${dokarkiv.url}")
    private lateinit var url: String
    private lateinit var dokArkivConsumer: DokArkivConsumer

    @Inject
    private lateinit var metrikk: Metrikk
    private lateinit var mockRestServiceServer: MockRestServiceServer

    @Before
    fun setUp() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
        Mockito.`when`(stsConsumer.token()).thenReturn("token")
        dokArkivConsumer = DokArkivConsumer(restTemplate, url, stsConsumer, metrikk)
    }

    @After
    fun tearDown() {
        mockRestServiceServer.verify()
    }

    @Test
    fun journalforOppfolgingsplan() {
        val responseBody = journalPostResponseAsJsonString()
        mockRestServiceServer.expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo("$url/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true"),
        )
            .andRespond(MockRestResponseCreators.withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(responseBody))
        val oppfolgingsplan = Oppfolgingsplan()
            .virksomhet(KAKEBUA)
            .arbeidstaker(KAKEMONSTERET)
            .sistEndretAvAktoerId(AKTOR_ID)
            .uuid(UUID.randomUUID().toString())
        val dokument = "dokument".toByteArray()
        val godkjentplan = GodkjentPlan()
            .dokument(dokument)
        val journalpostId = dokArkivConsumer.journalforOppfolgingsplan(oppfolgingsplan, godkjentplan)
        Assertions.assertThat(journalpostId).isEqualTo(JOURNALPOST_ID)
    }

    @Test(expected = RestClientException::class)
    @Throws(Exception::class)
    fun journalforOppfolgingsplanFeiler() {
        mockRestServiceServer.expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo("$url/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true"),
        )
            .andRespond(MockRestResponseCreators.withBadRequest())
        val oppfolgingsplan = Oppfolgingsplan()
            .virksomhet(KAKEBUA)
            .arbeidstaker(KAKEMONSTERET)
            .sistEndretAvAktoerId(AKTOR_ID)
        val dokument = "dokument".toByteArray()
        val godkjentplan = GodkjentPlan()
            .dokument(dokument)
        dokArkivConsumer.journalforOppfolgingsplan(oppfolgingsplan, godkjentplan)
    }

    private fun journalPostResponseAsJsonString(): String {
        val objectMapper = ObjectMapper()
        val module = JavaTimeModule()
        objectMapper.registerModule(module)
        val dokumentListe: MutableList<DokumentInfo> = ArrayList()
        val dokumentInfo = DokumentInfo()
        dokumentListe.add(dokumentInfo)
        val journalpostResponse = JournalpostResponse()
            .journalpostId(JOURNALPOST_ID)
            .journalpostferdigstilt(true)
            .journalstatus(JOURNAL_STATUS)
            .melding(null)
            .dokumenter(dokumentListe)
        return try {
            objectMapper.writeValueAsString(journalpostResponse)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val JOURNALPOST_ID = 123
        private const val AKTOR_ID = "K1234"
        private val KAKEBUA = Virksomhet()
            .navn("Kakebua")
            .virksomhetsnummer("123456789")
        private val KAKEMONSTERET = Person()
            .navn("Kake M. Onster")
            .fnr("01010100099")
            .aktoerId(AKTOR_ID)
        private const val JOURNAL_STATUS = "ENDELIG"
    }
}
