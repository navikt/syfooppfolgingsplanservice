package no.nav.syfo.azuread;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

@Component
public class AzureAdTokenClient {
    private static final Logger LOG = getLogger(AzureAdTokenClient.class);

    private final RestTemplate restTemplateMedProxy;
    private final String url;
    private final String clientId;
    private final String clientSecret;
    private volatile Map<String, AzureAdResponse> azureAdTokenMap = new HashMap<>();

    @Autowired
    public AzureAdTokenClient(
            @Qualifier("restTemplateMedProxy") RestTemplate restTemplateMedProxy,
            @Value("${azure.openid.config.token.endpoint}") String url,
            @Value("${azure.app.client.id}") String clientId,
            @Value("${azure.app.client.secret}") String clientSecret
    ) {
        this.restTemplateMedProxy = restTemplateMedProxy;
        this.url = url;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getAccessToken(String scope) {
        final Instant omToMinutter = Instant.now().plusSeconds(120L);
        final AzureAdResponse azureAdResponse = azureAdTokenMap.get(scope);

        LOG.info("Endpoint: [" + url + "] ClientId: [" + clientId + "] ClientSecret: [" + clientSecret + "]");

        if (azureAdResponse == null || azureAdResponse.expires_on().isBefore(omToMinutter)) {
            LOG.info("Henter nytt token fra Azure AD for scope {}", scope);
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("scope", scope);
            body.add("grant_type", "client_credentials");
            body.add("client_secret", clientSecret);

            final String uriString = UriComponentsBuilder.fromHttpUrl(url).toUriString();

            final ResponseEntity<AzureAdResponse> result = restTemplateMedProxy.exchange(
                    uriString,
                    POST,
                    new HttpEntity<>(body, headers),
                    AzureAdResponse.class
            );

            if (result.getStatusCode() != OK) {
                throw new RuntimeException("Henting av token fra Azure AD feiler med HTTP-" + result.getStatusCode());
            }
            azureAdTokenMap.put(scope, requireNonNull(result.getBody()));
        }
        return requireNonNull(azureAdTokenMap.get(scope)).access_token();
    }
}
