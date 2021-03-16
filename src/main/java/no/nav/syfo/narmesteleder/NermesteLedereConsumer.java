package no.nav.syfo.narmesteleder;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static no.nav.syfo.config.CacheConfig.CACHENAME_LEDER;
import static no.nav.syfo.util.CredentialUtilKt.bearerHeader;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.azuread.AzureAdTokenConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.NaermesteLederStatus;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.pdl.exceptions.NameFromPDLIsNull;

@Component
public class NermesteLedereConsumer {
    private static final Logger LOG = getLogger(NermesteLedereConsumer.class);

    private final AktorregisterConsumer aktorregisterConsumer;
    private final AzureAdTokenConsumer azureAdTokenConsumer;
    private final Metrikk metrikk;
    private final PdlConsumer pdlConsumer;
    private final RestTemplate restTemplate;
    private final String url;
    private final String syfonarmestelederId;

    public static final String HENT_LEDERE_SYFONARMESTELEDER = "hent_ledere_syfonarmesteleder";
    public static final String HENT_LEDERE_SYFONARMESTELEDER_FEILET = "hent_ledere_syfonarmesteleder_feilet";
    public static final String HENT_LEDERE_SYFONARMESTELEDER_VELLYKKET = "hent_leder_syfonarmesteleder_vellykket";

    public static final String ERROR_MESSAGE_BASE = "Kall mot syfonarmesteleder feiler med HTTP-";

    @Autowired
    public NermesteLedereConsumer(
            AktorregisterConsumer aktorregisterConsumer,
            AzureAdTokenConsumer azureAdTokenConsumer,
            Metrikk metrikk,
            PdlConsumer pdlConsumer,
            RestTemplate restTemplateMedProxy,
            @Value("${syfonarmesteleder.url}") String url,
            @Value("${syfonarmesteleder.id}") String syfonarmestelederId
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.azureAdTokenConsumer = azureAdTokenConsumer;
        this.metrikk = metrikk;
        this.pdlConsumer = pdlConsumer;
        this.restTemplate = restTemplateMedProxy;
        this.url = url;
        this.syfonarmestelederId = syfonarmestelederId;
    }

    @Cacheable(value = CACHENAME_LEDER, key = "#aktorId", condition = "#aktorId != null")
    public Optional<List<Naermesteleder>> nermesteLedere(String aktorId) {
        metrikk.tellHendelse(HENT_LEDERE_SYFONARMESTELEDER);
        String token = azureAdTokenConsumer.getAccessToken(syfonarmestelederId);

        ResponseEntity<List<NarmesteLederRelasjon>> response = restTemplate.exchange(
                getLedereUrl(aktorId),
                GET,
                entity(token),
                new ParameterizedTypeReference<List<NarmesteLederRelasjon>>(){}
        );

        throwExceptionIfError(response.getStatusCode(), HENT_LEDERE_SYFONARMESTELEDER_FEILET);

        if (Objects.requireNonNull(response.getBody()).isEmpty()) {
            return Optional.empty();
        }

        List<NarmesteLederRelasjon> relasjoner = response.getBody();
        List<Naermesteleder> nermesteLedere = new ArrayList<>();

        for (NarmesteLederRelasjon relasjon : relasjoner) {
            String lederAktorId = relasjon.narmesteLederAktorId;
            String lederFnr = aktorregisterConsumer.hentFnrForAktor(lederAktorId);
            String lederNavn = Optional.ofNullable(pdlConsumer.personName(lederFnr)).orElseThrow(() -> new NameFromPDLIsNull("Name of leader was null"));
            nermesteLedere.add(narmestelederRelasjon2Leder(relasjon, lederNavn));
        }

        metrikk.tellHendelse(HENT_LEDERE_SYFONARMESTELEDER_VELLYKKET);

        return Optional.of(nermesteLedere);
    }

    private void throwExceptionIfError(HttpStatus statusCode, String metricEventKey) {
        if (statusCode != OK) {
            metrikk.tellHendelse(metricEventKey);
            final String message = ERROR_MESSAGE_BASE + statusCode;
            LOG.error(message);
            throw new RuntimeException(message);
        }
    }

    private HttpEntity entity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(token));
        return new HttpEntity<>(headers);
    }

    private String getLedereUrl(String aktoerId) {
        return UriComponentsBuilder.fromHttpUrl(url + "/syfonarmesteleder/sykmeldt/" + aktoerId + "/narmesteledere").toUriString();
    }

    //todo: REFACTOR DUPLICATES
    private Naermesteleder narmestelederRelasjon2Leder(NarmesteLederRelasjon narmesteLederRelasjon, String lederNavn) {
        return new Naermesteleder()
                .epost(narmesteLederRelasjon.narmesteLederEpost)
                .mobil(narmesteLederRelasjon.narmesteLederTelefonnummer)
                .naermesteLederStatus(
                        new NaermesteLederStatus()
                                .erAktiv(narmesteLederRelasjon.aktivTom == null)
                                .aktivFom(narmesteLederRelasjon.aktivFom)
                                .aktivTom(narmesteLederRelasjon.aktivTom))
                .orgnummer(narmesteLederRelasjon.orgnummer)
                .navn(lederNavn)
                .naermesteLederAktoerId(narmesteLederRelasjon.narmesteLederAktorId);
    }
}
