package no.nav.syfo.narmesteleder;

import no.nav.syfo.azuread.AzureAdTokenConsumer;
import no.nav.syfo.model.Ansatt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

@RunWith(MockitoJUnitRunner.class)
public class NarmesteLederConsumerTest {
    @Mock
    private AzureAdTokenConsumer azureAdTokenConsumer;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NarmesteLederConsumer narmesteLederConsumer;

    private final String AKTOR_ID = "1234567890987";
    private final String VIRKSOMHETSNUMMER = "1234";

    @Test
    public void getAnsatte() {
        List<NarmesteLederRelasjon> narmesteLederRelasjoner = singletonList(
                new NarmesteLederRelasjon()
                        .aktorId(AKTOR_ID)
                        .orgnummer(VIRKSOMHETSNUMMER)
        );

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {}))).thenReturn(new ResponseEntity<>(narmesteLederRelasjoner, HttpStatus.OK));

        List<Ansatt> ansatte = narmesteLederConsumer.ansatte(AKTOR_ID);

        assertThat(ansatte.size()).isEqualTo(narmesteLederRelasjoner.size());
        assertThat(ansatte.get(0).aktoerId).isEqualTo(narmesteLederRelasjoner.get(0).aktorId);
        assertThat(ansatte.get(0).virksomhetsnummer).isEqualTo(narmesteLederRelasjoner.get(0).orgnummer);
    }

    @Test(expected = RuntimeException.class)
    public void runtimeException_hvis_ikke_OK_fra_syfoNarmesteLeder() {
        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {
        }))).thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

        List<Ansatt> ansatte = narmesteLederConsumer.ansatte(AKTOR_ID);
    }
}
