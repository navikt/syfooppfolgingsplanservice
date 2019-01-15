package no.nav.syfo.service;

import no.nav.syfo.domain.LoggMelding;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.OppfoelgingsdialogAltinn;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.RestUtils.basicCredentials;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class JuridiskLoggService {

    private static final Logger LOG = getLogger(JuridiskLoggService.class);

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
            LOG.info("Logget sending av oppfølgingsplan med id {} i juridisk loggSendOppfoelgingsdialogTilAltinn", oppfoelgingsdialog.id);
        } catch (HttpClientErrorException e) {
            LOG.error("Klientfeil mot JuridiskLogg ved logging av sendt oppfølgingsplan med id {} til Altinn", oppfoelgingsdialog.id, e);
            throw e;
        }
    }
}
