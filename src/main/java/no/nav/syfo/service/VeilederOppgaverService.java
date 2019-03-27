package no.nav.syfo.service;

import no.nav.syfo.model.VeilederOppgave;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static java.util.Collections.singletonMap;
import static no.nav.syfo.util.RestUtils.basicCredentials;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
public class VeilederOppgaverService {

    private String syfoveilederoppgaveUsername;
    private String syfoveilederoppgavePassword;

    public static final String FNR = "fnr";
    private static final String FNR_PLACEHOLDER = "{" + FNR + "}";
    private final RestTemplate template;
    private final UriComponentsBuilder hentVeilederoppgaverUriTemplate;

    public VeilederOppgaverService(
            RestTemplate template,
            @Value("${syfoveilederoppgaver.system.v1.url}") String syfoveilederoppgaveUrl,
            @Value("${syfoveilederoppgaver.systemapi.username}") String syfoveilederoppgaveUsername,
            @Value("${syfoveilederoppgaver.systemapi.password}") String syfoveilederoppgavePassword
    ) {
        this.syfoveilederoppgaveUsername = syfoveilederoppgaveUsername;
        this.syfoveilederoppgavePassword = syfoveilederoppgavePassword;
        this.template = template;
        hentVeilederoppgaverUriTemplate = fromHttpUrl(syfoveilederoppgaveUrl)
                .queryParam(FNR, FNR_PLACEHOLDER);
    }

    public List<VeilederOppgave> get(String fnr) {
        String credentials = basicCredentials(syfoveilederoppgaveUsername, syfoveilederoppgavePassword);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, credentials);
        HttpEntity request = new HttpEntity<>(headers);

        URI tilgangTilBrukerUriMedFnr = hentVeilederoppgaverUriTemplate.build(singletonMap(FNR, fnr));

        ResponseEntity<List<VeilederOppgave>> response = template.exchange(
                tilgangTilBrukerUriMedFnr,
                HttpMethod.GET,
                request, new ParameterizedTypeReference<List<VeilederOppgave>>() {
                }
        );

        return response.getBody();
    }
}
