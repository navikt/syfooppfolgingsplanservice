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
import static no.nav.syfo.narmesteleder.NarmesteLedereConsumer.HENT_LEDERE_SYFONARMESTELEDER;
import static no.nav.syfo.narmesteleder.NarmesteLedereConsumer.HENT_LEDERE_SYFONARMESTELEDER_VELLYKKET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.azuread.AzureAdTokenConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.pdl.PdlConsumer;

@RunWith(MockitoJUnitRunner.class)
public class NarmesteLedereConsumerTest {

    @Mock
    private AktorregisterConsumer aktorregisterConsumer;

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

    private final String SYKMELDT_AKTOR_ID = "1234567890987";
    private final String LEDER_AKTOR_ID = "7890987654321";
    private final String FNR = "12345678901";
    private final String FIRSTNAME = "Firstname";
    private final String MIDDLENAME = "Middlename";
    private final String SURNAME = "Surname";

    @Test
    public void getNarmesteLeder() {
        ReflectionTestUtils.setField(narmesteLedereConsumer, "url", "http://syfonarmesteleder.url");

        List<NarmesteLederRelasjon> narmesteLederRelasjoner = singletonList(
                new NarmesteLederRelasjon()
                        .aktorId(SYKMELDT_AKTOR_ID)
                        .narmesteLederAktorId(LEDER_AKTOR_ID)
        );

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {})))
                .thenReturn(new ResponseEntity<>(narmesteLederRelasjoner, OK));
        when(narmesteLederRelasjonConverter.convert(any(NarmesteLederRelasjon.class), anyString()))
                .thenReturn(new Naermesteleder()
                                    .naermesteLederAktoerId(LEDER_AKTOR_ID)
                                    .navn(pdlName()));
        when(aktorregisterConsumer.hentFnrForAktor(anyString())).thenReturn(FNR);
        when(pdlConsumer.personName(anyString())).thenReturn(pdlName());

        Optional<List<Naermesteleder>> naermestelederOptional = narmesteLedereConsumer.narmesteLedere(SYKMELDT_AKTOR_ID);
        assertThat(naermestelederOptional.isPresent()).isTrue();

        Naermesteleder naermesteleder = naermestelederOptional.get().get(0);

        assertThat(naermesteleder.naermesteLederAktoerId).isEqualTo(LEDER_AKTOR_ID);
        assertThat(naermesteleder.navn).isEqualTo(pdlName());

        verify(metrikk).tellHendelse(HENT_LEDERE_SYFONARMESTELEDER);
        verify(metrikk).tellHendelse(HENT_LEDERE_SYFONARMESTELEDER_VELLYKKET);
    }

    @Test
    public void empty_optional_when_no_object_from_syfonarmesteleder() {
        ReflectionTestUtils.setField(narmesteLedereConsumer, "url", "http://syfonarmesteleder.url");


        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {})))
                .thenReturn(new ResponseEntity<>(null, OK));

        Optional<List<Naermesteleder>> naermestelederOptional = narmesteLedereConsumer.narmesteLedere(SYKMELDT_AKTOR_ID);
        assertThat(naermestelederOptional.isPresent()).isFalse();


        verify(aktorregisterConsumer, never()).hentAktorIdForFnr(anyString());
        verify(pdlConsumer, never()).person(anyString());
    }

    private String pdlName() {
        return FIRSTNAME + MIDDLENAME + SURNAME;
    }
}
