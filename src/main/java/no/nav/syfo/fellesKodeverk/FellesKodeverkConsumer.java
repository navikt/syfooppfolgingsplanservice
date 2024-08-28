package no.nav.syfo.fellesKodeverk;

import no.nav.syfo.azuread.AzureAdTokenConsumer;
import no.nav.syfo.metric.Metrikk;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static no.nav.syfo.config.CacheConfig.CACHENAME_FELLESKODEVERK_BETYDNINGER;
import static no.nav.syfo.util.CredentialUtilKt.bearerHeader;
import static no.nav.syfo.util.RequestUtilKt.APP_CONSUMER_ID;
import static no.nav.syfo.util.RequestUtilKt.createCallId;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;

@Service
public class FellesKodeverkConsumer {
    private static final Logger LOG = getLogger(FellesKodeverkConsumer.class);

    private final Metrikk metric;
    private final RestTemplate restTemplateMedProxy;
    private final String felleskodeverkUrl;
    private final String felleskodeverkScope;

    private final AzureAdTokenConsumer azureAdTokenConsumer;

    public static final String NAV_CALL_ID_HEADER = "Nav-Call-Id";
    public static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";

    @Autowired
    public FellesKodeverkConsumer(
            Metrikk metric,
            RestTemplate restTemplateMedProxy,
            @Value("${felleskodeverk.url}") String felleskodeverkUrl,
            @Value("${felleskodeverk.scope}") String felleskodeverkScope,
            AzureAdTokenConsumer azureAdTokenConsumer
    ) {
        this.metric = metric;
        this.restTemplateMedProxy = restTemplateMedProxy;
        this.felleskodeverkUrl = felleskodeverkUrl;
        this.felleskodeverkScope = felleskodeverkScope;
        this.azureAdTokenConsumer = azureAdTokenConsumer;
    }

    @Cacheable(cacheNames = CACHENAME_FELLESKODEVERK_BETYDNINGER)
    public KodeverkKoderBetydningerResponse kodeverkKoderBetydninger() {
        String kodeverkYrkerBetydningUrl = felleskodeverkUrl + "/kodeverk/Yrker/koder/betydninger?spraak=nb";

        String accessToken = azureAdTokenConsumer.getAccessToken(felleskodeverkScope);

        try {
            ResponseEntity<KodeverkKoderBetydningerResponse> response = restTemplateMedProxy.exchange(
                    kodeverkYrkerBetydningUrl,
                    GET,
                    entity(accessToken),
                    KodeverkKoderBetydningerResponse.class
            );
            metric.tellHendelse("call_felleskodeverk_success");
            LOG.info("[AAD KODEVERK FUNKER]");
            return response.getBody();
        } catch (RestClientException e) {
            metric.tellHendelse("call_felleskodeverk_fail");
            LOG.error("Error from Felles Kodeverk with request-url: " + kodeverkYrkerBetydningUrl, e);
            throw new RuntimeException("Tried to get kodeBetydninger from Felles Kodeverk", e);
        }
    }

    private HttpEntity entity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(token));
        headers.add(NAV_CALL_ID_HEADER, createCallId());
        headers.add(NAV_CONSUMER_ID, APP_CONSUMER_ID);
        return new HttpEntity<>(headers);
    }
}
