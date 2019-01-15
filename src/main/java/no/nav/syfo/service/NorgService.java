package no.nav.syfo.service;

import no.nav.syfo.domain.Enhet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.HentOverordnetEnhetListeEnhetIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetRelasjonstyper;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetsstatus;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSHentOverordnetEnhetListeRequest;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class NorgService {
    private static final Logger LOG = getLogger(NorgService.class);

    @Inject
    private OrganisasjonEnhetV2 organisasjonEnhetV2;

    public Optional<Enhet> finnSetteKontor(String enhet) {
        try {
            return organisasjonEnhetV2.hentOverordnetEnhetListe(new WSHentOverordnetEnhetListeRequest()
                    .withEnhetId(enhet).withEnhetRelasjonstype(new WSEnhetRelasjonstyper().withValue("HABILITET")))
                    .getOverordnetEnhetListe()
                    .stream()
                    .filter(wsOrganisasjonsenhet -> WSEnhetsstatus.AKTIV.equals(wsOrganisasjonsenhet.getStatus()))
                    .map(wsOrganisasjonsenhet -> new Enhet().enhetId(wsOrganisasjonsenhet.getEnhetId()).navn(wsOrganisasjonsenhet.getEnhetNavn()))
                    .findFirst();
        } catch (HentOverordnetEnhetListeEnhetIkkeFunnet e) {
            LOG.error("Fant ingen overordnet enhet");
            throw new RuntimeException();
        }
    }
}
