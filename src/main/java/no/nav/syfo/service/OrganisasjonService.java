package no.nav.syfo.service;

import no.nav.tjeneste.virksomhet.organisasjon.v4.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.WSUstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.WSHentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.WSHentOrganisasjonResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;

import static java.util.stream.Collectors.joining;
import static org.slf4j.LoggerFactory.getLogger;

public class OrganisasjonService {
    private static final Logger LOG = getLogger(OrganisasjonService.class);

    @Inject
    private OrganisasjonV4 organisasjonV4;

    @Cacheable("ereg")
    public String finnVirksomhetsnavn(String orgnummer) {
        try {
            WSHentOrganisasjonResponse response = organisasjonV4.hentOrganisasjon(request(orgnummer));
            WSUstrukturertNavn ustrukturertNavn = (WSUstrukturertNavn) response.getOrganisasjon().getNavn();

            return ustrukturertNavn.getNavnelinje().stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(joining(", "));

        } catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput e) {
            LOG.error("Feil ved henting av Arbeidsgivers navn", e);
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
