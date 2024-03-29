package no.nav.syfo.sykmeldinger;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
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

import static no.nav.syfo.sykmeldinger.ArbeidstakerSykmeldingerConsumer.HENT_SYKMELDINGER_SYFOSMREGISTER;
import static no.nav.syfo.sykmeldinger.ArbeidstakerSykmeldingerConsumer.HENT_SYKMELDINGER_SYFOSMREGISTER_FEILET;
import static no.nav.syfo.sykmeldinger.ArbeidstakerSykmeldingerConsumer.HENT_SYKMELDINGER_SYFOSMREGISTER_VELLYKKET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Sykmelding;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.sykmeldinger.dto.ArbeidsgiverStatusDTO;
import no.nav.syfo.sykmeldinger.dto.BehandlingsutfallDTO;
import no.nav.syfo.sykmeldinger.dto.RegelStatusDTO;
import no.nav.syfo.sykmeldinger.dto.RegelinfoDTO;
import no.nav.syfo.sykmeldinger.dto.SykmeldingDTO;
import no.nav.syfo.sykmeldinger.dto.SykmeldingStatusDTO;
import no.nav.syfo.sykmeldinger.dto.SykmeldingsperiodeDTO;


@RunWith(MockitoJUnitRunner.class)
public class ArbeidstakerSykmeldingerConsumerTest {

    @Mock
    private PdlConsumer pdlConsumer;

    @Mock
    private Metrikk metrikk;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ArbeidstakerSykmeldingerConsumer arbeidstakerSykmeldingerConsumer;

    private final String ARBEIDSTAKER_AKTOR_ID = "0000000000000";

    @Test
    public void get_sendte_sykmeldinger() {
        ReflectionTestUtils.setField(arbeidstakerSykmeldingerConsumer, "syfosmregisterURL", "http://syfosmregister.url");

        BehandlingsutfallDTO behandlingsutfall = new BehandlingsutfallDTO()
                .status(RegelStatusDTO.OK)
                .ruleHits(List.of(new RegelinfoDTO().ruleName("").messageForSender("").messageForUser("").ruleStatus(RegelStatusDTO.OK)));

        BehandlingsutfallDTO behandlingsutfallInvalid = new BehandlingsutfallDTO()
                .status(RegelStatusDTO.INVALID)
                .ruleHits(List.of(new RegelinfoDTO().ruleName("").messageForSender("").messageForUser("").ruleStatus(RegelStatusDTO.INVALID)));

        List<SykmeldingsperiodeDTO> sykmeldingsperioder = List.of(new SykmeldingsperiodeDTO()
                                                                          .fom("2021-04-15")
                                                                          .tom("2021-04-30"));

        ArbeidsgiverStatusDTO arbeidsgiver = new ArbeidsgiverStatusDTO().orgnummer("orgnummer").juridiskOrgnummer(null).orgNavn("orgnavn");
        SykmeldingStatusDTO sykmeldingStatus = new SykmeldingStatusDTO().statusEvent("event").arbeidsgiver(arbeidsgiver);

        List<SykmeldingDTO> sykmeldingDTOList = List.of(new SykmeldingDTO()
                                                                .id("1")
                                                                .behandlingsutfall(behandlingsutfall)
                                                                .sykmeldingsperioder(sykmeldingsperioder)
                                                                .sykmeldingStatus(sykmeldingStatus),
                                                        new SykmeldingDTO()
                                                                .id("2")
                                                                .behandlingsutfall(behandlingsutfallInvalid)
                                                                .sykmeldingsperioder(sykmeldingsperioder)
                                                                .sykmeldingStatus(sykmeldingStatus));

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<SykmeldingDTO>>() {
        })))
                .thenReturn(new ResponseEntity<>(sykmeldingDTOList, OK));

        List<Sykmelding> sendteSykmeldinger = arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(ARBEIDSTAKER_AKTOR_ID, "123", false).orElseGet(List::of);

        assertThat(sendteSykmeldinger.size()).isNotEqualTo(sykmeldingDTOList.size());
        assertThat(sendteSykmeldinger.size()).isEqualTo(1);

        assertThat(sendteSykmeldinger.get(0).organisasjonsinformasjon().orgnummer()).isEqualTo(sykmeldingDTOList.get(0).sykmeldingStatus().arbeidsgiver.orgnummer());
        assertThat(sendteSykmeldinger.get(0).sykmeldingsperioder().get(0).fom).isEqualTo(
                sykmeldingDTOList.get(0).sykmeldingsperioder().get(0).fom);

        verify(metrikk).tellHendelse(HENT_SYKMELDINGER_SYFOSMREGISTER);
        verify(metrikk).tellHendelse(HENT_SYKMELDINGER_SYFOSMREGISTER_VELLYKKET);
    }

    @Test
    public void throws_runtimeException_when_syfosmregister_returns_not_OK() {
        ReflectionTestUtils.setField(arbeidstakerSykmeldingerConsumer, "syfosmregisterURL", "http://syfosmregister.url");

        when(restTemplate.exchange(anyString(), eq(GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<SykmeldingDTO>>() {
        })))
                .thenReturn(new ResponseEntity<>(null, INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(RuntimeException.class, () -> arbeidstakerSykmeldingerConsumer.getSendteSykmeldinger(ARBEIDSTAKER_AKTOR_ID, "456", false));

        verify(metrikk).tellHendelse(HENT_SYKMELDINGER_SYFOSMREGISTER);
        verify(metrikk).tellHendelse(HENT_SYKMELDINGER_SYFOSMREGISTER_FEILET);
    }
}
