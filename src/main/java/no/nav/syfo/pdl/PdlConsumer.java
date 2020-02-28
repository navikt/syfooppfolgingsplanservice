package no.nav.syfo.pdl;

import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.pdl.exceptions.*;
import no.nav.syfo.sts.StsConsumer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static no.nav.syfo.util.RestUtils.bearerHeader;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class PdlConsumer {
    private static final Logger LOG = getLogger(PdlConsumer.class);

    private final Metrikk metrikk;
    private final RestTemplate restTemplate;
    private final StsConsumer stsConsumer;
    private final String url;

    public static final String TEMA_HEADER = "Tema";
    public static final String ALLE_TEMA_HEADERVERDI = "GEN";
    public static final String NAV_CONSUMER_TOKEN_HEADER = "Nav-Consumer-Token";

    @Autowired
    public PdlConsumer(
            Metrikk metrikk,
            @Qualifier("scheduler") RestTemplate restTemplate,
            StsConsumer stsConsumer,
            @Value("${pdl.url}") String url
    ) {
        this.metrikk = metrikk;
        this.restTemplate = restTemplate;
        this.stsConsumer = stsConsumer;
        this.url = url;
    }

    @Cacheable(cacheNames = "personPdl", key = "#ident", condition = "#ident != null")
    public PdlHentPerson person(String ident) {
        metrikk.tellHendelse("call_pdl");

        String query = readFileAsString("pdl/hentPerson.graphql").replace("[\n\r]", "");

        HttpEntity<PdlRequest> entity = createRequestEntity(
                new PdlRequest()
                        .query(query)
                        .variables(new Variables().ident(ident))
        );

        final String uriString = UriComponentsBuilder.fromHttpUrl(url).toUriString();

        try {
            ResponseEntity<PdlPersonResponse> response = restTemplate.exchange(
                    uriString,
                    HttpMethod.POST,
                    entity,
                    PdlPersonResponse.class
            );
            metrikk.tellHendelse("call_pdl_success");
            PdlPersonResponse responseBody = response.getBody();
            throwExceptionIfPDLBodyHasErrorOrIsEmpty(responseBody);
            return responseBody.data;
        } catch (RestClientException e) {
            metrikk.tellHendelse("call_pdl_fail");
            LOG.error("Error from PDL with request-url: " + url, e);
            throw e;
        }
    }

    private void throwExceptionIfPDLBodyHasErrorOrIsEmpty(PdlPersonResponse responseBody) {
        if (responseBody == null) {
            metrikk.tellHendelse("call_pdl_fail");
            throw new EmptyPDLContent("Tried to get name of person :(");
        }
        if (responseBody.errors != null) {
            metrikk.tellHendelse("call_pdl_fail");
            throw new PDLResponseBodyContainsError(responseBody.errors.get(0).toSimplifiedString());
        }
        if (responseBody.data.getName() == null) {
            metrikk.tellHendelse("call_pdl_fail");
            throw new NameFromPDLIsNull("Tried to get name of person :( (SAD) :sadpanda:");
        }
    }

    private HttpEntity<PdlRequest> createRequestEntity(PdlRequest request) {
        String stsToken = stsConsumer.token();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TEMA_HEADER, ALLE_TEMA_HEADERVERDI);
        headers.set(AUTHORIZATION, bearerHeader(stsToken));
        headers.set(NAV_CONSUMER_TOKEN_HEADER, bearerHeader(stsToken));

        return new HttpEntity<>(request, headers);
    }

    private String readFileAsString(String path) {
        try {
            Resource resource = new ClassPathResource(path);
            InputStream input = resource.getInputStream();

            return IOUtils.toString(input, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            metrikk.tellHendelse("pdl_feil");
            LOG.error("Feilet ved lesing av hentPerson query", e);
            throw new RuntimeException("Feilet ved lesing av hentPerson query", e);
        }
    }
}
