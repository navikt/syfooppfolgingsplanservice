package no.nav.syfo.fellesKodeverk;

import no.nav.syfo.metric.Metrikk;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static no.nav.syfo.config.CacheConfig.CACHENAME_FELLESKODEVERK_BETYDNINGER;
import static no.nav.syfo.util.RequestUtilKt.APP_CONSUMER_ID;
import static no.nav.syfo.util.RequestUtilKt.createCallId;
import static no.nav.syfo.util.StringUtilKt.lowerCapitalize;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;

@Service
public class FellesKodeverkConsumer {
    private static final Logger LOG = getLogger(FellesKodeverkConsumer.class);

    private final Metrikk metric;
    private final RestTemplate restTemplateMedProxy;
    private final String url;

    public static final String NAV_CALL_ID_HEADER = "Nav-Call-Id";
    public static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";

    @Autowired
    public FellesKodeverkConsumer(
            Metrikk metric,
            RestTemplate restTemplateMedProxy,
            @Value("${felleskodeverk.url}") String url
    ) {
        this.metric = metric;
        this.restTemplateMedProxy = restTemplateMedProxy;
        this.url = url;
    }

    @Cacheable(cacheNames = CACHENAME_FELLESKODEVERK_BETYDNINGER)
    public KodeverkKoderBetydningerResponse kodeverkKoderBetydninger() {
        String kodeverkYrkerBetydningUrl = url + "/kodeverk/Yrker/koder/betydninger?spraak=nb";

        try {
            ResponseEntity<KodeverkKoderBetydningerResponse> response = restTemplateMedProxy.exchange(
                    kodeverkYrkerBetydningUrl,
                    GET,
                    entity(),
                    KodeverkKoderBetydningerResponse.class
            );
            metric.tellHendelse("call_felleskodeverk_success");
            return response.getBody();
        } catch (RestClientException e) {
            metric.tellHendelse("call_felleskodeverk_fail");
            LOG.error("Error from Felles Kodeverk with request-url: " + kodeverkYrkerBetydningUrl, e);
            throw new RuntimeException("Tried to get kodeBetydninger from Felles Kodeverk", e);
        }
    }

    public String stillingsnavnFromKode(String stillingskode) {
        KodeverkKoderBetydningerResponse response = kodeverkKoderBetydninger();
        try {
            String stillingsnavn = stillingsnavnFromKodeverkKoderBetydningerResponse(response, stillingskode);
            return lowerCapitalize(stillingsnavn);
        } catch (NullPointerException e) {
            LOG.error("Couldn't find navn for stillingskode: " + stillingskode);
            return "Ugyldig yrkeskode " + stillingskode;
        }
    }

    private String stillingsnavnFromKodeverkKoderBetydningerResponse(KodeverkKoderBetydningerResponse response, String stillingskode) {
        return response.betydninger.get(stillingskode).get(0).beskrivelser.get("nb").tekst;
    }

    private HttpEntity entity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(NAV_CALL_ID_HEADER, createCallId());
        headers.add(NAV_CONSUMER_ID, APP_CONSUMER_ID);
        return new HttpEntity<>(headers);
    }
}
