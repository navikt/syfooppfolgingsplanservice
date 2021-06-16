package no.nav.syfo.narmesteleder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static no.nav.syfo.narmesteleder.NarmesteLedereConsumer.HENT_LEDERE_NARMESTELEDER;
import static no.nav.syfo.narmesteleder.NarmesteLedereConsumer.HENT_LEDERE_NARMESTELEDER_VELLYKKET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import no.nav.syfo.azuread.AzureAdTokenConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.pdl.PdlConsumer;

@RunWith(MockitoJUnitRunner.class)
public class NarmesteLedereConsumerTest {

    @Mock
    private AzureAdTokenConsumer azureAdTokenConsumer;

    @Mock
    private NarmesteLederRelasjonConverter narmesteLederRelasjonConverter;

    @Mock
    private Metrikk metrikk;

    @Mock
    private PdlConsumer pdlConsumer;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NarmesteLedereConsumer narmesteLedereConsumer;

    private final String SYKMELDT_FNR = "10987654321";
    private final String LEDER_FNR = "12345678901";
    private final String NAVN = "Firstname";
    private final String MELLOMNAVN = "Middlename";
    private final String ETTERNAVN = "Surname";

    @Test
    public void not_empty_optional_when_object_is_returned_from_narmesteleder() {
        ReflectionTestUtils.setField(narmesteLedereConsumer, "narmestelederUrl", "http://narmesteleder.url");

        List<NarmesteLederRelasjon> narmesteLederRelasjoner = singletonList(
                new NarmesteLederRelasjon()
                        .fnr(SYKMELDT_FNR)
                        .narmesteLederFnr(LEDER_FNR)
        );

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {
        })))
                .thenReturn(new ResponseEntity<>(narmesteLederRelasjoner, OK));
        when(narmesteLederRelasjonConverter.convert(any(NarmesteLederRelasjon.class), anyString()))
                .thenReturn(new Naermesteleder()
                                    .naermesteLederFnr(LEDER_FNR)
                                    .navn(pdlName()));
        when(pdlConsumer.personName(anyString())).thenReturn(pdlName());

        Optional<List<Naermesteleder>> naermestelederOptional = narmesteLedereConsumer.narmesteLedere(SYKMELDT_FNR);
        assertThat(naermestelederOptional.isPresent()).isTrue();

        Naermesteleder naermesteleder = naermestelederOptional.get().get(0);

        assertThat(naermesteleder.naermesteLederFnr).isEqualTo(LEDER_FNR);
        assertThat(naermesteleder.navn).isEqualTo(pdlName());

        verify(metrikk).tellHendelse(HENT_LEDERE_NARMESTELEDER);
        verify(metrikk).tellHendelse(HENT_LEDERE_NARMESTELEDER_VELLYKKET);
    }

    @Test
    public void empty_optional_when_no_object_from_narmesteleder() {
        ReflectionTestUtils.setField(narmesteLedereConsumer, "narmestelederUrl", "http://narmesteleder.url");


        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {
        })))
                .thenReturn(new ResponseEntity<>(null, OK));

        Optional<List<Naermesteleder>> naermestelederOptional = narmesteLedereConsumer.narmesteLedere(SYKMELDT_FNR);
        assertThat(naermestelederOptional.isPresent()).isFalse();

        verify(pdlConsumer, never()).person(anyString());
    }

    private String pdlName() {
        return NAVN + MELLOMNAVN + ETTERNAVN;
    }
}
