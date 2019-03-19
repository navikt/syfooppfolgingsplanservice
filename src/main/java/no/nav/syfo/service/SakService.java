package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.sak.v1.*;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.WSFinnSakRequest;

import javax.inject.Inject;
import java.util.Optional;

@Slf4j
public class SakService {

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
            log.error("Det skjedde en feil", e);
            throw new RuntimeException("Sak finnes allerede", e);
        }
    }
}
