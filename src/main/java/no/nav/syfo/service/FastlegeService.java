package no.nav.syfo.service;

import no.nav.syfo.domain.rs.RSOppfoelgingsplan;
import no.nav.syfo.metric.Metrikk;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static no.nav.syfo.util.RequestUtilKt.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
public class FastlegeService {

    private static final Logger log = getLogger(FastlegeService.class);

    public static final String SEND_OPPFOLGINGSPLAN_PATH = "/sendOppfolgingsplanFraSelvbetjening";
    private final RestTemplate template;
    private final UriComponentsBuilder delMedFastlegeUriTemplate;
    private final Metrikk metrikk;

    public FastlegeService(
            @Value("${fastlege.dialogmelding.api.v1.url}") String dialogfordelerUrl,
            Metrikk metrikk,
            RestTemplate template
    ) {
        delMedFastlegeUriTemplate = fromHttpUrl(dialogfordelerUrl)
                .path(SEND_OPPFOLGINGSPLAN_PATH);
        this.metrikk = metrikk;
        this.template = template;
    }

    public void sendOppfolgingsplan(RSOppfoelgingsplan rsOppfoelgingsplan) {
        URI tilgangTilBrukerUriMedFnr = delMedFastlegeUriTemplate.build().toUri();

        kallUriMedTemplate(tilgangTilBrukerUriMedFnr, rsOppfoelgingsplan);

        log.info("Sendt oppfølgingsplan til dialogfordeler");
    }

    private void kallUriMedTemplate(URI uri, RSOppfoelgingsplan rsOppfoelgingsplan) {
        try {
            template.postForLocation(uri, entity(rsOppfoelgingsplan));
            tellPlanDeltMedFastlegeKall(true);
        } catch (HttpClientErrorException e) {
            int responsekode = e.getRawStatusCode();
            tellPlanDeltMedFastlegeKall(false);
            if (responsekode == 500) {
                throw new RuntimeException("Kunne ikke dele med fastlege");
            } else if (responsekode >= 300) {
                log.error("Feil ved sending av oppfølgingsdialog til fastlege: Fikk responskode " + responsekode);
                throw new RuntimeException("Feil ved sending av oppfølgingsdialog til fastlege: Fikk responskode " + responsekode);
            } else {
                throw e;
            }
        } catch (Exception e) {
            tellPlanDeltMedFastlegeKall(false);
            throw e;
        }
    }

    private HttpEntity<RSOppfoelgingsplan> entity(RSOppfoelgingsplan rsOppfoelgingsplan) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(NAV_CONSUMER_ID_HEADER, APP_CONSUMER_ID);
        headers.add(NAV_CALL_ID_HEADER, createCallId());
        return new HttpEntity<>(rsOppfoelgingsplan, headers);
    }

    private void tellPlanDeltMedFastlegeKall(boolean delt) {
        metrikk.tellHendelseMedTag("plan_delt_med_fastlege", "delt", delt);
    }
}
