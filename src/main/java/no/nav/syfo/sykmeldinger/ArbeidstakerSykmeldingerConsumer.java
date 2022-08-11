package no.nav.syfo.sykmeldinger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Organisasjonsinformasjon;
import no.nav.syfo.model.Sykmelding;
import no.nav.syfo.model.Sykmeldingsperiode;
import no.nav.syfo.sykmeldinger.dto.ArbeidsgiverStatusDTO;
import no.nav.syfo.sykmeldinger.dto.RegelStatusDTO;
import no.nav.syfo.sykmeldinger.dto.SykmeldingDTO;
import no.nav.syfo.sykmeldinger.dto.SykmeldingsperiodeDTO;

@Component
public class ArbeidstakerSykmeldingerConsumer {
    private static final Logger LOG = getLogger(ArbeidstakerSykmeldingerConsumer.class);

    public static final String ERROR_MESSAGE_BASE = "Kall mot syfosmregister feiler med HTTP-";

    public static final String HENT_SYKMELDINGER_SYFOSMREGISTER = "hent_sykmeldinger_syfosmregister";
    public static final String HENT_SYKMELDINGER_SYFOSMREGISTER_FEILET = "hent_sykmeldinger_syfosmregister_feilet";
    public static final String HENT_SYKMELDINGER_SYFOSMREGISTER_VELLYKKET = "hent_sykmeldinger_syfosmregister_vellykket";

    private final AktorregisterConsumer aktorregisterConsumer;

    private final Metrikk metrikk;
    private final RestTemplate restTemplate;
    private final String syfosmregisterURL;

    @Autowired
    public ArbeidstakerSykmeldingerConsumer(AktorregisterConsumer aktorregisterConsumer,
                                            Metrikk metrikk,
                                            RestTemplate restTemplateMedProxy,
                                            @Value("${syfosmregister.url}") String syfosmregisterURL) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.metrikk = metrikk;
        this.restTemplate = restTemplateMedProxy;
        this.syfosmregisterURL = syfosmregisterURL;
    }

    private static List<Sykmeldingsperiode> convertToSykmeldingsperiode(List<SykmeldingsperiodeDTO> sykmeldingsperiodeDTO) {
        return sykmeldingsperiodeDTO.stream()
                .map(dto -> new Sykmeldingsperiode().fom(toLocalDate(dto.fom)).tom(toLocalDate(dto.tom)))
                .collect(Collectors.toList());
    }

    private static LocalDate toLocalDate(String date) {
        return LocalDate.parse(date);
    }

    private static Organisasjonsinformasjon convertToOrganisasjonInformasjon(ArbeidsgiverStatusDTO arbeidsgiverStatusDTO) {
        return new Organisasjonsinformasjon().orgNavn(arbeidsgiverStatusDTO.orgNavn()).orgnummer(arbeidsgiverStatusDTO.orgnummer());
    }

    private String getSykmeldingerUrl(boolean isToday) {
        if (isToday) {
            LocalDate idag = LocalDate.now();
            String dato = idag.toString();
            return UriComponentsBuilder.fromHttpUrl(syfosmregisterURL + "/api/v3/sykmeldinger?include=SENDT" + "&fom=" + dato + "&tom=" + dato).toUriString();
        }
        return UriComponentsBuilder.fromHttpUrl(syfosmregisterURL + "/api/v3/sykmeldinger?include=SENDT").toUriString();
    }

    public Optional<List<Sykmelding>> getSendteSykmeldinger(String aktorId, String idToken, boolean isToday) {
        metrikk.tellHendelse(HENT_SYKMELDINGER_SYFOSMREGISTER);

        String fnr = aktorregisterConsumer.hentFnrForAktor(aktorId);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, idToken);
        headers.add("fnr", fnr);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<SykmeldingDTO>> response = restTemplate.exchange(
                getSykmeldingerUrl(isToday),
                GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        if (response.getStatusCode() != OK) {
            metrikk.tellHendelse(HENT_SYKMELDINGER_SYFOSMREGISTER_FEILET);
            final String message = ERROR_MESSAGE_BASE + response.getStatusCode();
            LOG.error(message);
            throw new RuntimeException(message);
        }

        if (Objects.requireNonNull(response).getBody() == null) {
            return Optional.empty();
        }

        metrikk.tellHendelse(HENT_SYKMELDINGER_SYFOSMREGISTER_VELLYKKET);
        return Optional.of(mapTilSykmeldingsliste(Objects.requireNonNull(response.getBody()), fnr));
    }

    private List<Sykmelding> mapTilSykmeldingsliste(List<SykmeldingDTO> sykmeldingerDTO, String fnr) {
        return sykmeldingerDTO.stream()
                .filter(dto -> dto.behandlingsutfall.status != RegelStatusDTO.INVALID)
                .map(dto -> new Sykmelding(dto.id,
                                           fnr,
                                           convertToSykmeldingsperiode(dto.sykmeldingsperioder),
                                           convertToOrganisasjonInformasjon(dto.sykmeldingStatus.arbeidsgiver)))
                .collect(Collectors.toUnmodifiableList());
    }
}
