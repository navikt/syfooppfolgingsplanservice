package no.nav.syfo.fellesKodeverk

import junit.framework.TestCase
import no.nav.syfo.azuread.AzureAdTokenConsumer
import no.nav.syfo.metric.Metrikk
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@RunWith(MockitoJUnitRunner::class)
class FellesKodeverkConsumerTest : TestCase() {
    @Mock
    private lateinit var metric: Metrikk

    @Mock
    private lateinit var restTemplate: RestTemplate

    @InjectMocks
    private lateinit var fellesKodeverkConsumer: FellesKodeverkConsumer

    @Mock
    private lateinit var azureAdTokenConsumer: AzureAdTokenConsumer

    @Test
    fun get_kodeverkKoderBetydninger() {
        val expectedResponse = fellesKodeverkResponseBody(STILLINGSNAVN, STILLINGSKODE)
        Mockito
            .`when`(
                restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity::class.java),
                    eq(KodeverkKoderBetydningerResponse::class.java),
                ),
            ).thenReturn(ResponseEntity(expectedResponse, HttpStatus.OK))
        val actualResponse = fellesKodeverkConsumer.kodeverkKoderBetydninger()
        Assertions.assertThat(actualResponse.betydninger[STILLINGSKODE]).isNotNull
        Assertions.assertThat(actualResponse!!.betydninger[STILLINGSKODE]?.get(0)!!.beskrivelser[SPRAK]).isNotNull
        Assertions
            .assertThat(
                actualResponse.betydninger[STILLINGSKODE]
                    ?.get(0)!!
                    .beskrivelser[SPRAK]!!
                    .tekst,
            ).isEqualTo(STILLINGSNAVN)
        Mockito.verify(metric).tellHendelse("call_felleskodeverk_success")
    }

    @Test
    fun kodeverKoderBetydninger_fail() {
        Mockito
            .`when`(
                restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity::class.java),
                    eq(KodeverkKoderBetydningerResponse::class.java),
                ),
            ).thenThrow(RestClientException("Something went wrong!"))
        try {
            fellesKodeverkConsumer.kodeverkKoderBetydninger()
        } catch (e: Exception) {
            Mockito.verify(metric).tellHendelse("call_felleskodeverk_fail")
            Assertions.assertThat(e.javaClass).isEqualTo(RuntimeException::class.java)
            Assertions.assertThat(e.message).isEqualTo("Tried to get kodeBetydninger from Felles Kodeverk")
        }
    }
}
