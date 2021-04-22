package no.nav.syfo.sykmeldinger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.syfo.config.CacheConfig.CACHENAME_SYKEMELDINGER;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.OrganisasjonsInformasjon;
import no.nav.syfo.model.Sykmelding;
import no.nav.syfo.model.Sykmeldingsperiode;
import no.nav.syfo.sykmeldinger.dto.ArbeidsgiverStatusDTO;
import no.nav.syfo.sykmeldinger.dto.RegelStatusDTO;
import no.nav.syfo.sykmeldinger.dto.SykmeldingDTO;
import no.nav.syfo.sykmeldinger.dto.SykmeldingsperiodeDTO;

@Component
public class SykmeldingerConsumer {
    private static final Logger LOG = getLogger(SykmeldingerConsumer.class);

    public static final String ERROR_MESSAGE_BASE = "Kall mot syfosmregister feiler med HTTP-";

    public static final String HENT_SYKMELDINGER_SYFOSMREGISTER = "hent_sykmeldinger_syfosmregister";
    public static final String HENT_SYKMELDINGER_SYFOSMREGISTER_FEILET = "hent_sykmeldinger_syfosmregister_feilet";
    public static final String HENT_SYKMELDINGER_SYFOSMREGISTER_VELLYKKET = "hent_sykmeldinger_syfosmregister_vellykket";

    private final AktorregisterConsumer aktorregisterConsumer;

    private final Metrikk metrikk;
    private final RestTemplate restTemplate;
    private final String syfosmregisterURL;

    @Autowired
    public SykmeldingerConsumer(AktorregisterConsumer aktorregisterConsumer,
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
                .map(dto -> new Sykmeldingsperiode().fom(dto.fom).tom(dto.tom))
                .collect(Collectors.toList());
    }

    private static OrganisasjonsInformasjon convertToOrganisasjonInformasjon(ArbeidsgiverStatusDTO arbeidsgiverStatusDTO) {
        return new OrganisasjonsInformasjon().orgNavn(arbeidsgiverStatusDTO.orgNavn()).orgnummer(arbeidsgiverStatusDTO.orgnummer());
    }

    public Optional<List<Sykmelding>> getSendteSykmeldinger(String aktorId, String idToken) {
        metrikk.tellHendelse(HENT_SYKMELDINGER_SYFOSMREGISTER);

        String fnr = aktorregisterConsumer.hentFnrForAktor(aktorId);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, idToken);
        headers.add("fnr", fnr);
        headers.setContentType(MediaType.APPLICATION_JSON);

        LOG.error("SMREG header tkn: {}", headers.getValuesAsList(HttpHeaders.AUTHORIZATION)); //TODO:
        ResponseEntity<List<SykmeldingDTO>> response = restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(syfosmregisterURL + "/api/v2/sykmeldinger/?include=SENDT").toUriString(),
                GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode() != OK) {
            metrikk.tellHendelse(HENT_SYKMELDINGER_SYFOSMREGISTER_FEILET);
            final String message = ERROR_MESSAGE_BASE + response.getStatusCode();
            LOG.info("SMREG response !=OK: {}", response.toString()); //TODO:
            LOG.error(message);
            throw new RuntimeException(message);
        }

        if (Objects.requireNonNull(response).getBody() == null) {
            LOG.warn("SMREG response.getBody er null, response: {}", response.toString()); //TODO:
            return Optional.empty();
        }

        metrikk.tellHendelse(HENT_SYKMELDINGER_SYFOSMREGISTER_VELLYKKET);
        LOG.warn("SMREG response er ok: {}", response.toString()); //TODO:
        return Optional.of(mapTilSykmeldingsliste(Objects.requireNonNull(response.getBody())));
    }

    private List<Sykmelding> mapTilSykmeldingsliste(List<SykmeldingDTO> sykmeldingerDTO) {
        return sykmeldingerDTO.stream()
                .filter(todo -> todo.behandlingsutfall.status != RegelStatusDTO.INVALID)
                .map(dto -> new Sykmelding(dto.id,
                                           convertToSykmeldingsperiode(dto.sykmeldingsperioder),
                                           convertToOrganisasjonInformasjon(dto.sykmeldingStatus.arbeidsgiver)))
                .collect(Collectors.toUnmodifiableList());
    }
}
