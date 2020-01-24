package no.nav.syfo.narmesteleder;

import no.nav.syfo.aareg.AaregConsumer;
import no.nav.syfo.aareg.Arbeidsforhold;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.azuread.AzureAdTokenConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.pdl.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static java.util.Collections.singletonList;
import static no.nav.syfo.aareg.utils.AaregConsumerTestUtils.*;
import static no.nav.syfo.narmesteleder.NarmesteLederConsumer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class NarmesteLederConsumerTest {

    @Mock
    private AaregConsumer aaregConsumer;

    @Mock
    private AktorregisterConsumer aktorregisterConsumer;

    @Mock
    private AzureAdTokenConsumer azureAdTokenConsumer;

    @Mock
    private Metrikk metrikk;

    @Mock
    private PdlConsumer pdlConsumer;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NarmesteLederConsumer narmesteLederConsumer;

    private final String SYKMELDT_AKTOR_ID = "1234567890987";
    private final String LEDER_AKTOR_ID = "7890987654321";
    private final String VIRKSOMHETSNUMMER = "123456789";
    private final String FNR = "12345678901";
    private final String FIRSTNAME = "Firstname";
    private final String MIDDLENAME = "Middlename";
    private final String SURNAME = "Surname";

    @Test
    public void getAnsatte() {
        List<NarmesteLederRelasjon> narmesteLederRelasjoner = singletonList(
                new NarmesteLederRelasjon()
                        .aktorId(SYKMELDT_AKTOR_ID)
                        .orgnummer(VIRKSOMHETSNUMMER)
        );

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {}))).thenReturn(new ResponseEntity<>(narmesteLederRelasjoner, OK));

        List<Ansatt> ansatte = narmesteLederConsumer.ansatte(SYKMELDT_AKTOR_ID);

        assertThat(ansatte.size()).isEqualTo(narmesteLederRelasjoner.size());
        assertThat(ansatte.get(0).aktoerId).isEqualTo(narmesteLederRelasjoner.get(0).aktorId);
        assertThat(ansatte.get(0).virksomhetsnummer).isEqualTo(narmesteLederRelasjoner.get(0).orgnummer);
        verify(metrikk).tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER);
        verify(metrikk).tellHendelse(HENT_ANSATTE_SYFONARMESTELEDER_VELLYKKET);
    }

    @Test
    public void runtimeException_hvis_ikke_OK_fra_syfoNarmesteLeder() {
        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {
        }))).thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            List<Ansatt> ansatte = narmesteLederConsumer.ansatte(SYKMELDT_AKTOR_ID);
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
        ReflectionTestUtils.setField(narmesteLederConsumer, "url", "http://syfonarmesteleder.url");

        NarmestelederResponse narmestelederResponse = new NarmestelederResponse().narmesteLederRelasjon(new NarmesteLederRelasjon()
                .aktorId(SYKMELDT_AKTOR_ID)
                .narmesteLederAktorId(LEDER_AKTOR_ID)
                .orgnummer(VIRKSOMHETSNUMMER));

        PdlHentPerson pdlHentPerson = mockPdlHentPerson();

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(NarmestelederResponse.class))).thenReturn(new ResponseEntity<>(narmestelederResponse, OK));
        when(aktorregisterConsumer.hentFnrForAktor(anyString())).thenReturn(FNR);
        when(pdlConsumer.person(anyString())).thenReturn(pdlHentPerson);

        Optional<Naermesteleder> naermestelederOptional = narmesteLederConsumer.narmesteLeder(SYKMELDT_AKTOR_ID, VIRKSOMHETSNUMMER);
        assertThat(naermestelederOptional.isPresent()).isTrue();

        Naermesteleder naermesteleder = naermestelederOptional.get();

        assertThat(naermesteleder.naermesteLederAktoerId).isEqualTo(LEDER_AKTOR_ID);
        assertThat(naermesteleder.orgnummer).isEqualTo(VIRKSOMHETSNUMMER);
        assertThat(naermesteleder.navn).isEqualTo(pdlHentPerson.getName());

        verify(metrikk).tellHendelse(HENT_LEDER_SYFONARMESTELEDER);
        verify(metrikk).tellHendelse(HENT_LEDER_SYFONARMESTELEDER_VELLYKKET);
    }

    @Test
    public void empty_optional_when_no_object_from_syfonarmesteleder() {
        ReflectionTestUtils.setField(narmesteLederConsumer, "url", "http://syfonarmesteleder.url");

        NarmestelederResponse narmestelederResponse = new NarmestelederResponse().narmesteLederRelasjon(null);

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(NarmestelederResponse.class))).thenReturn(new ResponseEntity<>(narmestelederResponse, OK));

        Optional<Naermesteleder> naermestelederOptional = narmesteLederConsumer.narmesteLeder(SYKMELDT_AKTOR_ID, VIRKSOMHETSNUMMER);
        assertThat(naermestelederOptional.isPresent()).isFalse();

        verify(aktorregisterConsumer, never()).hentAktorIdForFnr(anyString());
        verify(pdlConsumer, never()).person(anyString());
    }

    private PdlHentPerson mockPdlHentPerson() {
        return new PdlHentPerson()
                .hentPerson(
                        new PdlPerson()
                                .navn(singletonList(
                                        new PdlPersonNavn()
                                                .fornavn(FIRSTNAME)
                                                .mellomnavn(MIDDLENAME)
                                                .etternavn(SURNAME))));
    }
}