package no.nav.syfo.narmesteleder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static no.nav.syfo.narmesteleder.NarmesteLederConsumer.ERROR_MESSAGE_BASE;
import static no.nav.syfo.narmesteleder.NarmesteLederConsumer.HENT_ANSATTE_SYFONARMESTELEDER;
import static no.nav.syfo.narmesteleder.NarmesteLederConsumer.HENT_ANSATTE_SYFONARMESTELEDER_FEILET;
import static no.nav.syfo.narmesteleder.NarmesteLederConsumer.HENT_ANSATTE_SYFONARMESTELEDER_VELLYKKET;
import static no.nav.syfo.narmesteleder.NarmesteLederConsumer.HENT_LEDER_SYFONARMESTELEDER;
import static no.nav.syfo.narmesteleder.NarmesteLederConsumer.HENT_LEDER_SYFONARMESTELEDER_VELLYKKET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.pdl.PdlConsumer;

@RunWith(MockitoJUnitRunner.class)
public class NarmesteLederConsumerTest {

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
    private NarmesteLederConsumer narmesteLederConsumer;

    private final String VIRKSOMHETSNUMMER = "1234";
    private final String SYKMELDT_FNR = "10987654321";
    private final String LEDER_FNR = "12345678901";
    private final String FIRSTNAME = "Firstname";
    private final String MIDDLENAME = "Middlename";
    private final String SURNAME = "Surname";

    @Test
    public void getAnsatte() {
        List<NarmesteLederRelasjon> narmesteLederRelasjoner = singletonList(
                new NarmesteLederRelasjon()
                        .fnr(SYKMELDT_FNR)
                        .orgnummer(VIRKSOMHETSNUMMER)
        );

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {
        }))).thenReturn(new ResponseEntity<>(narmesteLederRelasjoner, OK));

        List<Ansatt> ansatte = narmesteLederConsumer.ansatte(LEDER_FNR);

        assertThat(ansatte.size()).isEqualTo(narmesteLederRelasjoner.size());
        assertThat(ansatte.get(0).fnr).isEqualTo(narmesteLederRelasjoner.get(0).fnr);
        assertThat(ansatte.get(0).virksomhetsnummer).isEqualTo(narmesteLederRelasjoner.get(0).orgnummer);
        verify(metrikk).tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER);
        verify(metrikk).tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER_VELLYKKET);
    }

    @Test
    public void runtimeException_hvis_ikke_OK_fra_syfoNarmesteLeder() {
        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {
        }))).thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            narmesteLederConsumer.ansatte(LEDER_FNR);
            fail("Skulle kastet RuntimeException!");
        } catch (RuntimeException expectedException) {
            assertThat(ERROR_MESSAGE_BASE + HttpStatus.INTERNAL_SERVER_ERROR).isEqualTo(expectedException.getMessage());
        } catch (Exception unexpectedException) {
            fail("Fikk en ukjent exception, det skulle vært RuntimeException!");
        }

        verify(metrikk).tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER);
        verify(metrikk).tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER_FEILET);
    }

    @Test
    public void getNarmesteLeder() {
        ReflectionTestUtils.setField(narmesteLederConsumer, "narmestelederUrl", "http://narmesteleder.url");

        NarmestelederResponse narmestelederResponse = new NarmestelederResponse().narmesteLederRelasjon(new NarmesteLederRelasjon()
                                                                                                                .fnr(SYKMELDT_FNR)
                                                                                                                .narmesteLederFnr(LEDER_FNR)
                                                                                                                .orgnummer(VIRKSOMHETSNUMMER));

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(NarmestelederResponse.class)))
                .thenReturn(new ResponseEntity<>(narmestelederResponse, OK));
        when(narmesteLederRelasjonConverter.convert(any(NarmesteLederRelasjon.class), anyString()))
                .thenReturn(new Naermesteleder()
                                    .naermesteLederFnr(LEDER_FNR)
                                    .orgnummer(VIRKSOMHETSNUMMER)
                                    .navn(pdlName()));
        when(pdlConsumer.personName(anyString())).thenReturn(pdlName());

        Optional<Naermesteleder> naermestelederOptional = narmesteLederConsumer.narmesteLeder(SYKMELDT_FNR, VIRKSOMHETSNUMMER);
        assertThat(naermestelederOptional.isPresent()).isTrue();

        Naermesteleder naermesteleder = naermestelederOptional.get();

        assertThat(naermesteleder.naermesteLederFnr).isEqualTo(LEDER_FNR);
        assertThat(naermesteleder.orgnummer).isEqualTo(VIRKSOMHETSNUMMER);
        assertThat(naermesteleder.navn).isEqualTo(pdlName());

        verify(metrikk).tellHendelse(HENT_LEDER_SYFONARMESTELEDER);
        verify(metrikk).tellHendelse(HENT_LEDER_SYFONARMESTELEDER_VELLYKKET);
    }

    @Test
    public void empty_optional_when_no_object_from_syfonarmesteleder() {
        ReflectionTestUtils.setField(narmesteLederConsumer, "narmestelederUrl", "http://narmesteleder.url");

        NarmestelederResponse narmestelederResponse = new NarmestelederResponse().narmesteLederRelasjon(null);

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(NarmestelederResponse.class))).thenReturn(
                new ResponseEntity<>(narmestelederResponse, OK));

        Optional<Naermesteleder> naermestelederOptional = narmesteLederConsumer.narmesteLeder(SYKMELDT_FNR, VIRKSOMHETSNUMMER);
        assertThat(naermestelederOptional.isPresent()).isFalse();

        verify(pdlConsumer, never()).person(anyString());
    }

    @Test
    public void erNaermesteLederForAnsatt(){
        ReflectionTestUtils.setField(narmesteLederConsumer, "narmestelederUrl", "http://narmesteleder.url");
        List<NarmesteLederRelasjon> narmesteLederRelasjoner = singletonList(
                new NarmesteLederRelasjon()
                        .fnr(SYKMELDT_FNR)
                        .orgnummer(VIRKSOMHETSNUMMER)
        );

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {
        }))).thenReturn(new ResponseEntity<>(narmesteLederRelasjoner, OK));
        boolean erNaermesteLederForAnsatt = narmesteLederConsumer.erNaermesteLederForAnsatt(LEDER_FNR, SYKMELDT_FNR);
        assertThat(erNaermesteLederForAnsatt).isTrue();
    }

    private String pdlName() {
        return FIRSTNAME + MIDDLENAME + SURNAME;
    }
}
