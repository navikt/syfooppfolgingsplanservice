package no.nav.syfo.brukertilgang;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oidc.OIDCUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.util.RestUtils.bearerHeader;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpMethod.GET;

@Service
public class BrukertilgangConsumer {
    private static final Logger LOG = getLogger(BrukertilgangConsumer.class);

    private final OIDCRequestContextHolder oidcContextHolder;
    private final Metrikk metrikk;
    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public BrukertilgangConsumer(
            OIDCRequestContextHolder oidcContextHolder,
            RestTemplate restTemplate,
            Metrikk metrikk,
            @Value("${syfobrukertilgang.url}") String baseUrl
    ) {
        this.oidcContextHolder = oidcContextHolder;
        this.restTemplate = restTemplate;
        this.metrikk = metrikk;
        this.baseUrl = baseUrl;
    }

    public boolean hasAccessToAnsatt(String ansattFnr) {
        metrikk.tellHendelse("call_syfobrukertilgang");
        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    arbeidstakerUrl(ansattFnr),
                    GET,
                    entity(),
                    new ParameterizedTypeReference<Boolean>() {
                    }
            );
            metrikk.tellHendelse("call_syfobrukertilgang_success");
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (HttpServerErrorException e) {
            metrikk.tellHendelse("call_syfobrukertilgang_fail");
            LOG.error("Error requesting ansatt access from syfobrukertilgang", e);
            throw e;
        }
    }

    private HttpEntity entity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(OIDCUtil.getIssuerToken(oidcContextHolder, EKSTERN)));
        return new HttpEntity<>(headers);
    }

    private String arbeidstakerUrl(String ansattFnr) {
        return baseUrl + "/api/v1/tilgang/ansatt/" + ansattFnr;
    }
}
