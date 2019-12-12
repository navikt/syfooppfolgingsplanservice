package no.nav.syfo.narmesteleder;

import no.nav.syfo.azuread.AzureAdTokenConsumer;
import no.nav.syfo.model.Ansatt;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.function.Function;

import static no.nav.syfo.config.cache.CacheConfig.CACHENAME_ANSATTE;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.RestUtils.bearerHeader;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.OK;

@Component
public class NarmesteLederConsumer {
    private static final Logger LOG = getLogger(NarmesteLederConsumer.class);

    private final AzureAdTokenConsumer azureAdTokenConsumer;
    private final RestTemplate restTemplate;
    private final String url;
    private final String syfonarmestelederId;

    @Autowired
    public NarmesteLederConsumer(
            AzureAdTokenConsumer azureAdTokenConsumer,
            RestTemplate restTemplateMedProxy,
            @Value("${syfonarmesteleder.url}") String url,
            @Value("${syfonarmesteleder.id}") String syfonarmestelederId
    ) {
        this.azureAdTokenConsumer = azureAdTokenConsumer;
        this.restTemplate = restTemplateMedProxy;
        this.url = url;
        this.syfonarmestelederId = syfonarmestelederId;
    }

    @Cacheable(value = CACHENAME_ANSATTE, key = "#aktorId", condition = "#aktorId != null")
    public List<Ansatt> ansatte(String aktorId) {
        String token = azureAdTokenConsumer.getAccessToken(syfonarmestelederId);

        ResponseEntity<List<NarmesteLederRelasjon>> response = restTemplate.exchange(
                getAnsatteUrl(aktorId),
                HttpMethod.GET,
                entity(token),
                new ParameterizedTypeReference<List<NarmesteLederRelasjon>>() {
                }
        );

        if (response.getStatusCode() != OK) {
            final String message = "Kall mot syfonarmesteleder feiler med HTTP-" + response.getStatusCode();
            LOG.error(message);
            throw new RuntimeException(message);
        }

        return mapListe(response.getBody(), narmestelederRelasjon2Ansatt);
    }

    private HttpEntity entity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(token));
        return new HttpEntity<>(headers);
    }

    private String getAnsatteUrl(String aktoerId) {
        return url + "/syfonarmesteleder/narmesteLeder/" + aktoerId;
    }

    private static Function<NarmesteLederRelasjon, Ansatt> narmestelederRelasjon2Ansatt = narmesteLederRelasjon ->
            new Ansatt()
                    .aktoerId(narmesteLederRelasjon.aktorId)
                    .virksomhetsnummer(narmesteLederRelasjon.orgnummer);
}
