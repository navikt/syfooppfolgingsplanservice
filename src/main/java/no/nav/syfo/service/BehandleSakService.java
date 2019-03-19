package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.behandlesak.v1.*;
import no.nav.tjeneste.virksomhet.behandlesak.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.behandlesak.v1.meldinger.WSOpprettSakRequest;

import javax.inject.Inject;

@Slf4j
public class BehandleSakService {

    @Inject
    private BehandleSakV1 behandleSakV1;

    public String opprettSak(String fnr) {
        try {
            return behandleSakV1.opprettSak(new WSOpprettSakRequest().withSak(new WSSak()
                            .withSakstype(new WSSakstyper().withValue("GEN"))
                            .withFagomraade(new WSFagomraader().withValue("OPP"))
                            .withFagsystem(new WSFagsystemer().withValue("FS22"))
                            .withGjelderBrukerListe(new WSPerson().withIdent(fnr))
                    )
            ).getSakId();
        } catch (OpprettSakSakEksistererAllerede e) {
            log.error("Sak finnes allerede", e);
            throw new RuntimeException("Sak finnes allerede", e);
        } catch (OpprettSakUgyldigInput e) {
            log.error("Ugyldig input", e);
            throw new RuntimeException("Ugyldid input i sak", e);
        }
    }
}
