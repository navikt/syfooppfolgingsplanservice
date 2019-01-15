package no.nav.syfo.service;

import no.nav.tjeneste.virksomhet.sak.v1.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.FinnSakUgyldigInput;
import no.nav.tjeneste.virksomhet.sak.v1.SakV1;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.WSFagomraader;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.WSFagsystemer;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.WSPerson;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.WSSak;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.WSFinnSakRequest;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class SakService {
    private static final Logger LOG = getLogger(SakService.class);

    @Inject
    private SakV1 sakV1;

    public Optional<String> finnSak(String fnr) {
        try {
            return sakV1.finnSak(new WSFinnSakRequest()
                    .withBruker(new WSPerson().withIdent(fnr))
                    .withFagomraadeListe(new WSFagomraader().withValue("OPP"))
                    .withFagsystem(new WSFagsystemer().withValue("FS22"))
            ).getSakListe().stream()
                    .filter(wsSak -> wsSak.getSakstype().getValue().equals("GEN"))
                    .map(WSSak::getSakId)
                    .findFirst();
        } catch (FinnSakForMangeForekomster | FinnSakUgyldigInput | RuntimeException e) {
            LOG.error("Det skjedde en feil", e);
            throw new RuntimeException("Sak finnes allerede", e);
        }
    }
}
