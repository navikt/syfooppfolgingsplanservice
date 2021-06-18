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

import no.nav.syfo.azuread.AzureAdTokenConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.pdl.exceptions.NameFromPDLIsNull;

@Component
public class NarmesteLedereConsumer {
    private static final Logger LOG = getLogger(NarmesteLedereConsumer.class);

    private final AzureAdTokenConsumer azureAdTokenConsumer;
    private final NarmesteLederRelasjonConverter narmesteLederRelasjonConverter;
    private final Metrikk metrikk;
    private final PdlConsumer pdlConsumer;
    private final RestTemplate restTemplate;
    private final String narmestelederUrl;
    private final String narmestelederScope;

    public static final String HENT_LEDERE_NARMESTELEDER = "hent_ledere_narmesteleder";
    public static final String HENT_LEDERE_NARMESTELEDER_FEILET = "hent_ledere_narmesteleder_feilet";
    public static final String HENT_LEDERE_NARMESTELEDER_VELLYKKET = "hent_ledere_narmesteleder_vellykket";

    public static final String ERROR_MESSAGE_BASE = "Kall mot narmesteleder feiler med HTTP-";

    @Autowired
    public NarmesteLedereConsumer(
            AzureAdTokenConsumer azureAdTokenConsumer,
            NarmesteLederRelasjonConverter narmesteLederRelasjonConverter,
            Metrikk metrikk,
            PdlConsumer pdlConsumer,
            RestTemplate restTemplateMedProxy,
            @Value("${narmesteleder.url}") String narmestelederUrl,
            @Value("${narmesteleder.scope}") String narmestelederScope
    ) {
        this.azureAdTokenConsumer = azureAdTokenConsumer;
        this.narmesteLederRelasjonConverter = narmesteLederRelasjonConverter;
        this.metrikk = metrikk;
        this.pdlConsumer = pdlConsumer;
        this.restTemplate = restTemplateMedProxy;
        this.narmestelederUrl = narmestelederUrl;
        this.narmestelederScope = narmestelederScope;
    }

    @Cacheable(value = CACHENAME_LEDER, key = "#fnr", condition = "#fnr != null")
    public Optional<List<Naermesteleder>> narmesteLedere(String fnr) {
        metrikk.tellHendelse(HENT_LEDERE_NARMESTELEDER);
        String token = azureAdTokenConsumer.getAccessToken(narmestelederScope);

        ResponseEntity<List<NarmesteLederRelasjon>> response = restTemplate.exchange(
                getLedereUrl(),
                GET,
                entity(token, fnr),
                new ParameterizedTypeReference<>() {
                }
        );

         throwExceptionIfError(response.getStatusCode(), HENT_LEDERE_NARMESTELEDER_FEILET);

        if (Objects.requireNonNull(response).getBody() == null) {
            return Optional.empty();
        }

        List<NarmesteLederRelasjon> relasjoner = response.getBody();
        List<Naermesteleder> narmesteLedere = new ArrayList<>();

        for (NarmesteLederRelasjon relasjon : relasjoner) {
            String lederFnr = relasjon.narmesteLederFnr;
            String lederNavn = Optional.ofNullable(pdlConsumer.personName(lederFnr)).orElseThrow(() -> new NameFromPDLIsNull("Name of leader was null"));
            narmesteLedere.add(narmesteLederRelasjonConverter.convert(relasjon, lederNavn));
        }

        metrikk.tellHendelse(HENT_LEDERE_NARMESTELEDER_VELLYKKET);

        return Optional.of(narmesteLedere);
    }

    private void throwExceptionIfError(HttpStatus statusCode, String metricEventKey) {
        if (statusCode != OK) {
            metrikk.tellHendelse(metricEventKey);
            final String message = ERROR_MESSAGE_BASE + statusCode;
            LOG.error(message);
            throw new RuntimeException(message);
        }
    }

    private HttpEntity entity(String token, String sykmeldtFnr) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(token));
        headers.add("Sykmeldt-Fnr", sykmeldtFnr);
        return new HttpEntity<>(headers);
    }

    private String getLedereUrl() {
        return UriComponentsBuilder.fromHttpUrl(narmestelederUrl + "/sykmeldt/narmesteledere").toUriString();
    }
}
