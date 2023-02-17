package no.nav.syfo.aareg;

import no.nav.syfo.fellesKodeverk.FellesKodeverkConsumer;
import no.nav.syfo.aareg.exceptions.RestErrorFromAareg;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.sts.StsConsumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static no.nav.syfo.util.CredentialUtilKt.bearerHeader;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;

@Service
public class AaregConsumer {
    private static final Logger LOG = getLogger(AaregConsumer.class);
    private final Metrikk metrikk;
    private final RestTemplate restTemplate;
    private final StsConsumer stsConsumer;
    private final String url;

    public static final String NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token";
    public static final String NAV_PERSONIDENT_HEADER = "Nav-Personident";

    @Autowired
    public AaregConsumer(
            Metrikk metrikk,
            RestTemplate restTemplate,
            StsConsumer stsConsumer,
            @Value("${aareg.services.url}") String url
    ) {
        this.metrikk = metrikk;
        this.restTemplate = restTemplate;
        this.stsConsumer = stsConsumer;
        this.url = url;
    }

    @Cacheable(cacheNames = "arbeidsforholdAT", key = "#fnr", condition = "#fnr != null")
    public List<Arbeidsforhold> arbeidsforholdArbeidstaker(String fnr) {
        metrikk.tellHendelse("call_aareg");
        String token = stsConsumer.token();

        try {
            ResponseEntity<List<Arbeidsforhold>> response = restTemplate.exchange(
                    arbeidstakerUrl(),
                    GET,
                    entity(fnr, token),
                    new ParameterizedTypeReference<>() {
                    }
            );
            metrikk.tellHendelse("call_aareg_success");
            return response.getBody();
        } catch (RestClientException e) {
            metrikk.tellHendelse("call_aareg_fail");
            LOG.error("Error from AAREG with request-url: " + url, e);
            throw new RestErrorFromAareg("Tried to get arbeidsforhold for arbeidstaker", e);
        }
    }

    private HttpEntity entity(String fnr, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(token));
        headers.add(NAV_CONSUMER_TOKEN_HEADER, bearerHeader(token));
        headers.add(NAV_PERSONIDENT_HEADER, fnr);
        return new HttpEntity<>(headers);
    }

    private String arbeidstakerUrl() {
        return url + "/v1/arbeidstaker/arbeidsforhold";
    }
}
