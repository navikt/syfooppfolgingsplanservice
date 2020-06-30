package no.nav.syfo.service;

import no.nav.syfo.domain.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static no.nav.syfo.util.RestUtils.basicCredentials;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class JuridiskLoggService {

    private static final Logger log = getLogger(JuridiskLoggService.class);

    @Value("${lagrejuridisklogg.rest.url}")
    private String altinnUrl;

    @Value("${srv.username}")
    private String altinnUsername;

    @Value("${srv.password}")
    private String systemPassword;

    public void loggSendOppfoelgingsdialogTilAltinn(OppfolgingsplanAltinn oppfolgingsplanAltinn) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanAltinn.oppfolgingsplan;
        try {
            LoggMelding loggMelding = new LoggMelding()
                    .meldingsId(oppfolgingsplan.uuid)
                    .avsender(oppfolgingsplan.arbeidstaker.aktoerId)
                    .mottaker(oppfolgingsplan.virksomhet.virksomhetsnummer)
                    .meldingsInnhold(oppfolgingsplanAltinn.getHashOppfoelgingsdialogPDF());

            RestTemplate rt = new RestTemplate();
            rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            rt.getMessageConverters().add(new StringHttpMessageConverter());

            String credentials = basicCredentials(altinnUsername, systemPassword);
            HttpHeaders headers = new HttpHeaders();
            headers.add(AUTHORIZATION, credentials);
            HttpEntity<LoggMelding> requestPost = new HttpEntity<>(loggMelding, headers);

            rt.exchange(altinnUrl, HttpMethod.POST, requestPost, LoggMelding.class);
            log.info("Logget sending av oppfølgingsplan med id {} i juridisk loggSendOppfoelgingsdialogTilAltinn", oppfolgingsplan.id);
        } catch (HttpClientErrorException e) {
            log.error("Klientfeil mot JuridiskLogg ved logging av sendt oppfølgingsplan med id {} til Altinn", oppfolgingsplan.id, e);
            throw e;
        }
    }
}
