package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.organisasjon.v4.*;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.WSUstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.WSHentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.WSHentOrganisasjonResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static java.util.stream.Collectors.joining;

@Slf4j
@Service
public class OrganisasjonService {

    private OrganisasjonV4 organisasjonV4;

    @Inject
    public OrganisasjonService(OrganisasjonV4 organisasjonV4) {
        this.organisasjonV4 = organisasjonV4;
    }

    public String finnVirksomhetsnavn(String orgnummer) {
        try {
            WSHentOrganisasjonResponse response = organisasjonV4.hentOrganisasjon(request(orgnummer));
            WSUstrukturertNavn ustrukturertNavn = (WSUstrukturertNavn) response.getOrganisasjon().getNavn();

            return ustrukturertNavn.getNavnelinje().stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(joining(", "));

        } catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput e) {
            log.error("Feil ved henting av Arbeidsgivers navn", e);
            throw new RuntimeException("Feil ved henting av Arbeidsgivers navn", e);
        }
    }

    private WSHentOrganisasjonRequest request(String orgnr) {
        return new WSHentOrganisasjonRequest()
                .withOrgnummer(orgnr)
                .withInkluderHierarki(false)
                .withInkluderHistorikk(false);
    }
}
