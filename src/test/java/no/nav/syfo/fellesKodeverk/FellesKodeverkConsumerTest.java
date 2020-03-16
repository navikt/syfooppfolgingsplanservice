package no.nav.syfo.fellesKodeverk;

import junit.framework.TestCase;
import no.nav.syfo.fellesKodeverk.exceptions.MissingStillingsnavn;
import no.nav.syfo.metric.Metrikk;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class FellesKodeverkConsumerTest extends TestCase {
    @Mock
    private Metrikk metric;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FellesKodeverkConsumer fellesKodeverkConsumer;

    private static final String STILLINGSNAVN = "Special Agent";
    private static final String WRONG_STILLINGSNAVN = "Deputy Director";
    private static final String STILLINGSKODE = "1234567";
    private static final String WRONG_STILLINGSKODE = "9876543";
    private static final String SPRAK = "nb";

    @Test
    public void stillingsnavnFromKode_gives_correct_stillingsnavn() {
        KodeverkKoderBetydningerResponse expectedResponse = responseBody();
        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(KodeverkKoderBetydningerResponse.class))).thenReturn(new ResponseEntity<>(expectedResponse, OK));

        String actualStillingsnavn = fellesKodeverkConsumer.stillingsnavnFromKode(STILLINGSKODE);

        assertThat(actualStillingsnavn).isEqualTo(STILLINGSNAVN);

        verify(metric).tellHendelse("call_felleskodeverk_success");
    }

    @Test(expected = MissingStillingsnavn.class)
    public void stillingsnavnFromKode_throws_exception_if_navn_not_found() {
        KodeverkKoderBetydningerResponse expectedResponse = responseBodyWithWrongKode();
        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(KodeverkKoderBetydningerResponse.class))).thenReturn(new ResponseEntity<>(expectedResponse, OK));

        fellesKodeverkConsumer.stillingsnavnFromKode(STILLINGSKODE);

        verify(metric).tellHendelse("call_felleskodeverk_success");
    }

    @Test
    public void get_kodeverkKoderBetydninger() {
        KodeverkKoderBetydningerResponse expectedResponse = responseBody();
        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(KodeverkKoderBetydningerResponse.class))).thenReturn(new ResponseEntity<>(expectedResponse, OK));

        KodeverkKoderBetydningerResponse actualResponse = fellesKodeverkConsumer.kodeverkKoderBetydninger();

        assertThat(actualResponse.betydninger.get(STILLINGSKODE)).isNotNull();
        assertThat(actualResponse.betydninger.get(STILLINGSKODE).get(0).beskrivelser.get(SPRAK)).isNotNull();
        assertThat(actualResponse.betydninger.get(STILLINGSKODE).get(0).beskrivelser.get(SPRAK).tekst).isEqualTo(STILLINGSNAVN);

        verify(metric).tellHendelse("call_felleskodeverk_success");
    }


    @Test
    public void kodeverKoderBetydninger_fail() {
        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(KodeverkKoderBetydningerResponse.class))).thenThrow(new RestClientException("Something went wrong!"));

        try {
            fellesKodeverkConsumer.kodeverkKoderBetydninger();
        } catch (Exception e) {
            verify(metric).tellHendelse("call_felleskodeverk_fail");
            assertThat(e.getClass()).isEqualTo(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Tried to get kodeBetydninger from Felles Kodeverk");
        }
    }

    private KodeverkKoderBetydningerResponse responseBody() {
        Beskrivelse beskrivelse = new Beskrivelse()
                .tekst(STILLINGSNAVN)
                .term(STILLINGSNAVN);

        Map<String, Beskrivelse> beskrivelser = new HashMap<>();
        beskrivelser.put(SPRAK, beskrivelse);

        Betydning betydning = new Betydning()
                .beskrivelser(beskrivelser)
                .gyldigFra(new Date().toString())
                .gyldigTil(new Date().toString());

        Map<String, List<Betydning>> betydninger = new HashMap<>();
        betydninger.put(STILLINGSKODE, singletonList(betydning));

        return new KodeverkKoderBetydningerResponse()
                .betydninger(betydninger);
    }

    private KodeverkKoderBetydningerResponse responseBodyWithWrongKode() {
        Beskrivelse beskrivelse = new Beskrivelse()
                .tekst(WRONG_STILLINGSNAVN)
                .term(WRONG_STILLINGSNAVN);

        Map<String, Beskrivelse> beskrivelser = new HashMap<>();
        beskrivelser.put(SPRAK, beskrivelse);

        Betydning betydning = new Betydning()
                .beskrivelser(beskrivelser)
                .gyldigFra(new Date().toString())
                .gyldigTil(new Date().toString());

        Map<String, List<Betydning>> betydninger = new HashMap<>();
        betydninger.put(WRONG_STILLINGSKODE, singletonList(betydning));

        return new KodeverkKoderBetydningerResponse()
                .betydninger(betydninger);
    }
}
