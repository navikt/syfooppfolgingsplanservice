package no.nav.syfo.sts;

import no.nav.syfo.metric.Metrikk;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static no.nav.syfo.sts.StsToken.setExpirationTime;
import static no.nav.syfo.util.RestUtils.basicCredentials;

@Service
public class StsConsumer {

    private final Metrikk metrikk;
    private final String password;
    private final RestTemplate restTemplate;
    private final String url;
    private final String username;

    private StsToken cachedOidcToken = null;

    @Autowired
    public StsConsumer(
            Metrikk metrikk,
            @Value("${srv.password}") String password,
            @Qualifier("scheduler") RestTemplate restTemplate,
            @Value("${security.token.service.rest.url}") String url,
            @Value("${srv.username}") String username
    ) {
        this.metrikk = metrikk;
        this.password = password;
        this.restTemplate = restTemplate;
        this.url = url;
        this.username = username;
    }

    public String token() {
        if (StsToken.shouldRenew(cachedOidcToken)) {
            metrikk.tellHendelse("call_sts");

            HttpEntity request = new HttpEntity<>(authorizationHeader());

            try {
                ResponseEntity<StsToken> response = restTemplate.exchange(
                        getStsTokenUrl(),
                        HttpMethod.GET,
                        request,
                        StsToken.class
                );
                StsToken token = response.getBody();
                setExpirationTime(token);
                cachedOidcToken = response.getBody();
                metrikk.tellHendelse("call_sts_success");
            } catch (HttpClientErrorException e) {
                metrikk.tellHendelse("call_sts_fail");
                throw e;
            }
        }
        return cachedOidcToken.access_token;
    }

    private String getStsTokenUrl() {
        return url + "/rest/v1/sts/token?grant_type=client_credentials&scope=openid";
    }

    private HttpHeaders authorizationHeader() {
        String credentials = basicCredentials(username, password);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, credentials);
        return headers;
    }
}
