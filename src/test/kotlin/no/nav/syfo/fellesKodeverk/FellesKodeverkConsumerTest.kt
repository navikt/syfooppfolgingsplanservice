package no.nav.syfo.fellesKodeverk

import junit.framework.TestCase
import no.nav.syfo.metric.Metrikk
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.http.*
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class FellesKodeverkConsumerTest : TestCase() {
    @Mock
    private lateinit var metric: Metrikk

    @Mock
    private lateinit var restTemplate: RestTemplate

    @InjectMocks
    private lateinit var fellesKodeverkConsumer: FellesKodeverkConsumer

    @Test
    fun stillingsnavnFromKode_gives_correct_stillingsnavn() {
        val expectedResponse = responseBody()
        Mockito.`when`(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity::class.java), ArgumentMatchers.eq(KodeverkKoderBetydningerResponse::class.java))).thenReturn(ResponseEntity(expectedResponse, HttpStatus.OK))
        val actualStillingsnavn = fellesKodeverkConsumer.stillingsnavnFromKode(STILLINGSKODE)
        Assertions.assertThat(actualStillingsnavn).isEqualTo(STILLINGSNAVN_LOWERCAPITALIZED)
        Mockito.verify(metric).tellHendelse("call_felleskodeverk_success")
    }

    @Test
    fun stillingsnavnFromKode_return_custom_message_if_navn_not_found() {
        val expectedResponse = responseBodyWithWrongKode()
        Mockito.`when`(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity::class.java), ArgumentMatchers.eq(KodeverkKoderBetydningerResponse::class.java))).thenReturn(ResponseEntity(expectedResponse, HttpStatus.OK))
        val actualStillingsnavn = fellesKodeverkConsumer.stillingsnavnFromKode(STILLINGSKODE)
        Assertions.assertThat(actualStillingsnavn).isEqualTo("Ugyldig yrkeskode $STILLINGSKODE")
        Mockito.verify(metric).tellHendelse("call_felleskodeverk_success")
    }

    @Test
    fun get_kodeverkKoderBetydninger() {
        val expectedResponse = responseBody()
        Mockito.`when`(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity::class.java), ArgumentMatchers.eq(KodeverkKoderBetydningerResponse::class.java))).thenReturn(ResponseEntity(expectedResponse, HttpStatus.OK))
        val actualResponse = fellesKodeverkConsumer.kodeverkKoderBetydninger()
        Assertions.assertThat(actualResponse.betydninger[STILLINGSKODE]).isNotNull
        Assertions.assertThat(actualResponse!!.betydninger[STILLINGSKODE]?.get(0)!!.beskrivelser[SPRAK]).isNotNull
        Assertions.assertThat(actualResponse.betydninger[STILLINGSKODE]?.get(0)!!.beskrivelser[SPRAK]!!.tekst).isEqualTo(STILLINGSNAVN)
        Mockito.verify(metric).tellHendelse("call_felleskodeverk_success")
    }

    @Test
    fun kodeverKoderBetydninger_fail() {
        Mockito.`when`(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity::class.java), ArgumentMatchers.eq(KodeverkKoderBetydningerResponse::class.java))).thenThrow(RestClientException("Something went wrong!"))
        try {
            fellesKodeverkConsumer.kodeverkKoderBetydninger()
        } catch (e: Exception) {
            Mockito.verify(metric).tellHendelse("call_felleskodeverk_fail")
            Assertions.assertThat(e.javaClass).isEqualTo(RuntimeException::class.java)
            Assertions.assertThat(e.message).isEqualTo("Tried to get kodeBetydninger from Felles Kodeverk")
        }
    }

    private fun responseBody(): KodeverkKoderBetydningerResponse {
        val beskrivelse = Beskrivelse()
            .tekst(STILLINGSNAVN)
            .term(STILLINGSNAVN)
        val beskrivelser: MutableMap<String, Beskrivelse> = HashMap()
        beskrivelser[SPRAK] = beskrivelse
        val betydning = Betydning()
            .beskrivelser(beskrivelser)
            .gyldigFra(Date().toString())
            .gyldigTil(Date().toString())
        val betydninger: MutableMap<String, List<Betydning>> = HashMap()
        betydninger[STILLINGSKODE] = listOf(betydning)
        return KodeverkKoderBetydningerResponse()
            .betydninger(betydninger)
    }

    private fun responseBodyWithWrongKode(): KodeverkKoderBetydningerResponse {
        val beskrivelse = Beskrivelse()
            .tekst(WRONG_STILLINGSNAVN)
            .term(WRONG_STILLINGSNAVN)
        val beskrivelser: MutableMap<String, Beskrivelse> = HashMap()
        beskrivelser[SPRAK] = beskrivelse
        val betydning = Betydning()
            .beskrivelser(beskrivelser)
            .gyldigFra(Date().toString())
            .gyldigTil(Date().toString())
        val betydninger: MutableMap<String, List<Betydning>> = HashMap()
        betydninger[WRONG_STILLINGSKODE] = listOf(betydning)
        return KodeverkKoderBetydningerResponse()
            .betydninger(betydninger)
    }

    companion object {
        private const val STILLINGSNAVN = "Special Agent"
        private const val STILLINGSNAVN_LOWERCAPITALIZED = "Special agent"
        private const val WRONG_STILLINGSNAVN = "Deputy Director"
        private const val STILLINGSKODE = "1234567"
        private const val WRONG_STILLINGSKODE = "9876543"
        private const val SPRAK = "nb"
    }
}
