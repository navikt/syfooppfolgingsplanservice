package no.nav.syfo.aktorregister;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.sts.StsConsumer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;

import static no.nav.syfo.aktorregister.AktorregisterUtils.currentIdentFromAktorregisterResponse;
import static no.nav.syfo.config.cache.CacheConfig.CACHENAME_AKTOR_FNR;
import static no.nav.syfo.config.cache.CacheConfig.CACHENAME_AKTOR_ID;
import static no.nav.syfo.util.RestUtils.bearerHeader;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
public class AktorregisterConsumer implements InitializingBean {

    private static AktorregisterConsumer instance;

    @Override
    public void afterPropertiesSet() {
        instance = this;
    }

    public static AktorregisterConsumer aktorregisterConsumer() {
        return instance;
    }

    private final String clientId;
    private final Metrikk metrikk;
    private final RestTemplate restTemplate;
    private final StsConsumer stsConsumer;
    private final String url;

    private static final String NAV_CONSUMER_ID_HEADER = "Nav-Consumer-Id";
    private static final String NAV_CALL_ID_HEADER = "Nav-Call-Id";
    private static final String NAV_PERSONIDENTER_HEADER = "Nav-Personidenter";

    public static final String IDENT_GROUP_FNR = "NorskIdent";
    public static final String IDENT_GROUP_AKTOR_ID = "AktoerId";

    @Inject
    public AktorregisterConsumer(
            @Value("${client.id}") String clientId,
            Metrikk metrikk,
            RestTemplate restTemplate,
            StsConsumer stsConsumer,
            @Value("${aktorregister.rest.url}") String url
    ) {
        this.clientId = clientId;
        this.metrikk = metrikk;
        this.restTemplate = restTemplate;
        this.stsConsumer = stsConsumer;
        this.url = url;
    }

    @Cacheable(value = CACHENAME_AKTOR_ID, key = "#fnr", condition = "#fnr != null")
    public String hentAktorIdForFnr(String fnr) {
        metrikk.tellHendelse("kall_aktorregister");
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            metrikk.tellHendelse("kall_aktorregister_feil");
            log.error("Prøvde å hente aktorId");
            throw new RuntimeException();
        }

        AktorregisterResponse response = getIdenterFromAktorregister(fnr);

        return currentIdentFromAktorregisterResponse(response, fnr, IDENT_GROUP_AKTOR_ID);
    }

    @Cacheable(value = CACHENAME_AKTOR_FNR, key = "#aktorId", condition = "#aktorId != null")
    public String hentFnrForAktor(String aktorId) {
        if (isBlank(aktorId) || !aktorId.matches("\\d{13}$")) {
            log.error("Prøvde å hente fnr med aktorId {}", aktorId);
            throw new RuntimeException();
        }

        AktorregisterResponse response = getIdenterFromAktorregister(aktorId);

        return currentIdentFromAktorregisterResponse(response, aktorId, IDENT_GROUP_FNR);
    }

    private AktorregisterResponse getIdenterFromAktorregister(String ident) {
        HttpEntity<String> entity = createRequestEntity(ident);

        final String uriString = UriComponentsBuilder.fromHttpUrl(url + "/identer").queryParam("gjeldende", true).toUriString();

        try {
            ResponseEntity<AktorregisterResponse> response = restTemplate.exchange(
                    uriString,
                    HttpMethod.GET,
                    entity,
                    AktorregisterResponse.class
            );
            metrikk.tellHendelse("aktorregister_suksess");
            return response.getBody();
        } catch (RestClientException e) {
            metrikk.tellHendelse("aktorregister_feil");
            log.error("Error from Aktorregisteret with request-url: " + url, e);
            throw e;
        } catch (Exception e) {
            metrikk.tellHendelse("aktorregister_feil");
            log.error("Uventet feil fra Aktorregisteret! med url: " + url, e);
            throw new RuntimeException("Uventet feil fra PDL!", e);
        }
    }

    private HttpEntity<String> createRequestEntity(String ident) {
        String stsToken = stsConsumer.token();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, bearerHeader(stsToken));
        headers.set(NAV_CONSUMER_ID_HEADER, clientId);
        headers.set(NAV_CALL_ID_HEADER, "123");
        headers.set(NAV_PERSONIDENTER_HEADER, ident);

        return new HttpEntity<>(headers);
    }
}
