package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.rs.RSOppfoelgingsplan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Slf4j
@Service
public class FastlegeService {

    public static final String SEND_OPPFOLGINGSPLAN_PATH = "/sendOppfolgingsplan";
    private final RestTemplate template;
    private final UriComponentsBuilder delMedFastlegeUriTemplate;

    public FastlegeService(
            @Value("${fastlege.dialogmelding.api.v1.url}") String dialogfordelerUrl,
            RestTemplate template
    ) {
        delMedFastlegeUriTemplate = fromHttpUrl(dialogfordelerUrl)
                .path(SEND_OPPFOLGINGSPLAN_PATH);
        this.template = template;
    }

    public void sendOppfolgingsplan(RSOppfoelgingsplan rsOppfoelgingsplan) {
        URI tilgangTilBrukerUriMedFnr = delMedFastlegeUriTemplate.build().toUri();

        HttpEntity<RSOppfoelgingsplan> request = new HttpEntity<>(rsOppfoelgingsplan);

        kallUriMedTemplate(tilgangTilBrukerUriMedFnr, request);

        log.info("Sendt oppfølgingsplan til dialogfordeler");
    }

    private void kallUriMedTemplate(URI uri, HttpEntity request) {
        try {
            template.exchange(uri, HttpMethod.POST, request, RSOppfoelgingsplan.class);
        } catch (HttpClientErrorException e) {
            int responsekode = e.getRawStatusCode();
            if (responsekode == 500) {
                throw new RuntimeException("Kunne ikke dele med fastlege");
            } else if (responsekode >= 300) {
                log.error("Feil ved sending av oppfølgingsdialog til fastlege: Fikk responskode " + responsekode);
                throw new RuntimeException("Feil ved sending av oppfølgingsdialog til fastlege: Fikk responskode " + responsekode);
            } else {
                throw e;
            }
        }
    }
}
