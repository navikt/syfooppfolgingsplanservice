package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.*;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.RestUtils.basicCredentials;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
public class JuridiskLoggService {

    public void loggSendOppfoelgingsdialogTilAltinn(OppfoelgingsdialogAltinn oppfoelgingsdialogAltinn) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelgingsdialogAltinn.oppfoelgingsdialog;
        try {
            LoggMelding loggMelding = new LoggMelding()
                    .meldingsId(oppfoelgingsdialog.uuid)
                    .avsender(oppfoelgingsdialog.arbeidstaker.aktoerId)
                    .mottaker(oppfoelgingsdialog.virksomhet.virksomhetsnummer)
                    .meldingsInnhold(oppfoelgingsdialogAltinn.getHashOppfoelgingsdialogPDF());

            RestTemplate rt = new RestTemplate();
            rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            rt.getMessageConverters().add(new StringHttpMessageConverter());

            String url = getProperty("JURIDISKLOGG-LAGRE_URL");

            String credentials = basicCredentials("srvserviceoppfoelgingsdialog");
            HttpHeaders headers = new HttpHeaders();
            headers.add(AUTHORIZATION, credentials);
            HttpEntity<LoggMelding> requestPost = new HttpEntity<>(loggMelding, headers);

            rt.exchange(url, HttpMethod.POST, requestPost, LoggMelding.class);
            log.info("Logget sending av oppfølgingsplan med id {} i juridisk loggSendOppfoelgingsdialogTilAltinn", oppfoelgingsdialog.id);
        } catch (HttpClientErrorException e) {
            log.error("Klientfeil mot JuridiskLogg ved logging av sendt oppfølgingsplan med id {} til Altinn", oppfoelgingsdialog.id, e);
            throw e;
        }
    }
}
