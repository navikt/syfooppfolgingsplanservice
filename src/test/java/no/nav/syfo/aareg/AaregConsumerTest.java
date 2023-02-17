package no.nav.syfo.aareg;

import no.nav.syfo.fellesKodeverk.FellesKodeverkConsumer;
import no.nav.syfo.aareg.exceptions.RestErrorFromAareg;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Stilling;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.sts.StsConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

import static no.nav.syfo.aareg.utils.AaregConsumerTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class AaregConsumerTest {
    @Mock
    private Metrikk metrikk;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private StsConsumer stsConsumer;

    @InjectMocks
    private AaregConsumer aaregConsumer;

    private static final String AAREG_URL = "http://aareg-services.url";

    @Before
    public void setup() {
        ReflectionTestUtils.setField(aaregConsumer, "url", AAREG_URL);
        when(stsConsumer.token()).thenReturn("token");
    }

    @Test
    public void getArbeidsforholdArbeidstaker() {
        List<Arbeidsforhold> expectedArbeidsforholdList = Collections.singletonList(simpleArbeidsforhold());
        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<Arbeidsforhold>>() {
        }))).thenReturn(new ResponseEntity<>(expectedArbeidsforholdList, OK));

        List<Arbeidsforhold> actualArbeidsforholdList = aaregConsumer.arbeidsforholdArbeidstaker(AT_FNR);

        assertThat(actualArbeidsforholdList.size()).isEqualTo(1);

        Arbeidsforhold arbeidsforhold = actualArbeidsforholdList.get(0);

        assertThat(arbeidsforhold.arbeidsgiver.organisasjonsnummer).isEqualTo(ORGNUMMER);
        assertThat(arbeidsforhold.arbeidstaker.aktoerId).isEqualTo(AT_AKTORID);
        assertThat(arbeidsforhold.arbeidstaker.offentligIdent).isEqualTo(AT_FNR);

        verify(metrikk).tellHendelse("call_aareg");
        verify(metrikk).tellHendelse("call_aareg_success");
    }

    @Test(expected = RestErrorFromAareg.class)
    public void arbeidsforholdArbeidstaker_fail() {
        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<Arbeidsforhold>>() {
        }))).thenThrow(new RestClientException("Failed!"));

        aaregConsumer.arbeidsforholdArbeidstaker(AT_FNR);
    }
}
