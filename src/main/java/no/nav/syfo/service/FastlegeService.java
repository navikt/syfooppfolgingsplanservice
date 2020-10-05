package no.nav.syfo.service;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.domain.rs.RSOppfoelgingsplan;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.oidc.OIDCIssuer;
import no.nav.syfo.oidc.OIDCUtil;
import no.nav.syfo.sts.StsConsumer;
import no.nav.syfo.util.InnsendingFeiletException;
import no.nav.syfo.util.OppslagFeiletException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static no.nav.syfo.util.CredentialUtilKt.bearerHeader;
import static no.nav.syfo.util.RequestUtilKt.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
public class FastlegeService {

    private static final Logger log = getLogger(FastlegeService.class);

    public static final String SEND_OPPFOLGINGSPLAN_PATH = "/sendOppfolgingsplanFraSelvbetjening";
    public static final String SEND_OPPFOLGINGSPLAN_LPS_PATH = "/oppfolgingsplan/lps";
    private final OIDCRequestContextHolder oidcContextHolder;
    private final RestTemplate template;
    private final UriComponentsBuilder delMedFastlegeUriTemplate;
    private final UriComponentsBuilder delLPSMedFastlegeUriTemplate;
    private final Metrikk metrikk;
    private final StsConsumer stsConsumer;

    public FastlegeService(
            @Value("${fastlege.dialogmelding.api.v1.url}") String dialogfordelerUrl,
            Metrikk metrikk,
            OIDCRequestContextHolder oidcContextHolder,
            StsConsumer stsConsumer,
            @Qualifier("scheduler") RestTemplate template
    ) {
        delMedFastlegeUriTemplate = fromHttpUrl(dialogfordelerUrl)
                .path(SEND_OPPFOLGINGSPLAN_PATH);
        delLPSMedFastlegeUriTemplate = fromHttpUrl(dialogfordelerUrl)
                .path(SEND_OPPFOLGINGSPLAN_LPS_PATH);
        this.metrikk = metrikk;
        this.oidcContextHolder = oidcContextHolder;
        this.stsConsumer = stsConsumer;
        this.template = template;
    }

    public void sendOppfolgingsplan(String sendesTilFnr, byte[] pdf) {
        RSOppfoelgingsplan rsOppfoelgingsplan = new RSOppfoelgingsplan(sendesTilFnr, pdf);
        URI tilgangTilBrukerUriMedFnr = delMedFastlegeUriTemplate.build().toUri();

        String token = OIDCUtil.getIssuerToken(oidcContextHolder, OIDCIssuer.EKSTERN);

        kallUriMedTemplate(
                tilgangTilBrukerUriMedFnr,
                rsOppfoelgingsplan,
                token,
                false
        );

        log.info("Sendt oppfølgingsplan til dialogfordeler");
    }

    public void sendOppfolgingsplanLPS(String sendesTilFnr, byte[] pdf) {
        RSOppfoelgingsplan rsOppfoelgingsplan = new RSOppfoelgingsplan(sendesTilFnr, pdf);
        URI tilgangTilBrukerUriMedFnr = delLPSMedFastlegeUriTemplate.build().toUri();
        String token = stsConsumer.token();

        kallUriMedTemplate(
                tilgangTilBrukerUriMedFnr,
                rsOppfoelgingsplan,
                token,
                true
        );
        log.info("Sendt oppfølgingsplanLPS til dialogfordeler");
    }

    private void kallUriMedTemplate(URI uri, RSOppfoelgingsplan rsOppfoelgingsplan, String token, boolean lps) {
        try {
            template.postForLocation(uri, entity(rsOppfoelgingsplan, token));
            tellPlanDeltMedFastlegeKall(lps, true);
        } catch (HttpClientErrorException e) {
            int responsekode = e.getRawStatusCode();
            log.error("Feil ved sending av oppfølgingsdialog til fastlege: Fikk responskode " + responsekode);
            tellPlanDeltMedFastlegeKall(lps, false);
            if(responsekode == 404) {
                throw new OppslagFeiletException("Feil ved oppslag av av fastlege eller partnerinformasjon");
            } else if (responsekode >= 300) {
                throw new RuntimeException("RuntimeException: Feil ved sending av oppfølgingsdialog til fastlege: Fikk responskode " + responsekode);
            } else {
                throw e;
            }
        } catch (HttpServerErrorException e) {
            int responsekode = e.getRawStatusCode();
            log.error("Feil ved sending av oppfølgingsdialog til fastlege: Fikk responskode " + responsekode);
            tellPlanDeltMedFastlegeKall(lps, false);
            throw new InnsendingFeiletException("Kunne ikke dele med fastlege");
        } catch (Exception e) {
            tellPlanDeltMedFastlegeKall(lps, false);
            throw e;
        }
    }

    private HttpEntity<RSOppfoelgingsplan> entity(RSOppfoelgingsplan rsOppfoelgingsplan, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, bearerHeader(token));
        headers.add(NAV_CONSUMER_ID_HEADER, APP_CONSUMER_ID);
        headers.add(NAV_CALL_ID_HEADER, createCallId());
        return new HttpEntity<>(rsOppfoelgingsplan, headers);
    }

    private void tellPlanDeltMedFastlegeKall(boolean lps, boolean delt) {
        if(lps) metrikk.tellHendelseMedTag("lps_plan_delt_med_fastlege", "delt", delt);
        metrikk.tellHendelseMedTag("plan_delt_med_fastlege", "delt", delt);
    }
}
