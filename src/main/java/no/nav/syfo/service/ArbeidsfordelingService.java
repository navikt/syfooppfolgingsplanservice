package no.nav.syfo.service;

import no.nav.syfo.domain.Enhet;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSArbeidsfordelingKriterier;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSGeografi;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSTema;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest;
import org.slf4j.Logger;

import javax.inject.Inject;

import static no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSEnhetsstatus.AKTIV;
import static org.slf4j.LoggerFactory.getLogger;

public class ArbeidsfordelingService {

    private static final Logger LOG = getLogger(ArbeidsfordelingService.class);

    @Inject
    private ArbeidsfordelingV1 arbeidsfordelingV1;

    public Enhet finnBehandlendeEnhet(String geografiskTilknytning) {
        try {
            return arbeidsfordelingV1.finnBehandlendeEnhetListe(new WSFinnBehandlendeEnhetListeRequest()
                    .withArbeidsfordelingKriterier(new WSArbeidsfordelingKriterier()
                            .withGeografiskTilknytning(new WSGeografi().withValue(geografiskTilknytning))
                            .withTema(new WSTema().withValue("OPP"))))
                    .getBehandlendeEnhetListe()
                    .stream()
                    .filter(wsOrganisasjonsenhet -> AKTIV.equals(wsOrganisasjonsenhet.getStatus()))
                    .map(wsOrganisasjonsenhet -> new Enhet().enhetId(wsOrganisasjonsenhet.getEnhetId()).navn(wsOrganisasjonsenhet.getEnhetNavn()))
                    .findFirst().orElse(new Enhet().enhetId(geografiskTilknytning).navn(geografiskTilknytning));
        } catch (FinnBehandlendeEnhetListeUgyldigInput e) {
            LOG.error("Feil ved henting av brukers forvaltningsenhet med geografiskTilknytning: {}", geografiskTilknytning, e);
            throw new RuntimeException("Feil ved henting av brukers forvaltningsenhet", e);
        } catch (RuntimeException e) {
            LOG.error("Feil ved henting av behandlende enhet for geografiskTilknytning {}", geografiskTilknytning, e);
            throw e;
        }
    }
}
