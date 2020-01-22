package no.nav.syfo.aktorregister;

import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.sts.StsConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class AktorregisterConsumerTest {

    @Mock
    private Metrikk metrikk;

    @Mock
    @Qualifier("scheduler") private RestTemplate restTemplate;

    @Mock
    private StsConsumer stsConsumer;

    @InjectMocks
    private AktorregisterConsumer aktorregisterConsumer;

    private final ParameterizedTypeReference<Map<String, IdentinfoListe>> RESPONSE_MAP_STRING = new ParameterizedTypeReference<Map<String, IdentinfoListe>>() {};
    private static final String KALL_AKTORREGISTER = "kall_aktorregister";
    private static final String FNR = "10108000390";
    private static final String AKTORID = "1234567898765";
    private static final String AREG_URL = "http://aktorregister.url";
    private static final String CLIENT_ID = "syfooppfolgings";

    @Test
    public void hentAktorIdForFnr() {
        ReflectionTestUtils.setField(aktorregisterConsumer, "url", AREG_URL);
        ReflectionTestUtils.setField(aktorregisterConsumer, "clientId", CLIENT_ID);
        mockStsConsumerToken();
        mockAregResponse(FNR);

        String aktorId = aktorregisterConsumer.hentAktorIdForFnr(FNR);

        assertThat(aktorId).isEqualTo(AKTORID);
        verify(metrikk).tellHendelse(KALL_AKTORREGISTER);
    }

    @Test
    public void hentFnrForAktor() {
        ReflectionTestUtils.setField(aktorregisterConsumer, "url", AREG_URL);
        ReflectionTestUtils.setField(aktorregisterConsumer, "clientId", CLIENT_ID);
        mockStsConsumerToken();
        mockAregResponse(AKTORID);

        String fnr = aktorregisterConsumer.hentFnrForAktor(AKTORID);

        assertThat(fnr).isEqualTo(FNR);
        verify(metrikk).tellHendelse(KALL_AKTORREGISTER);
    }

    private void mockStsConsumerToken() {
        when(stsConsumer.token()).thenReturn("The tremendously best token!");
    }

    private void mockAregResponse(String requestIdent) {
        Map<String, IdentinfoListe> identMap = new HashMap<>();
        List<Identinfo> identer = new ArrayList<>();
        identer.add(new Identinfo().ident(AKTORID).identgruppe("AktoerId").gjeldende(true));
        identer.add(new Identinfo().ident(FNR).identgruppe("NorskIdent").gjeldende(true));
        IdentinfoListe identinfoListe = new IdentinfoListe().identer(identer);
        identMap.put(requestIdent, identinfoListe);
        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(RESPONSE_MAP_STRING)))
                .thenReturn(new ResponseEntity<>(identMap, OK));
    }

}
