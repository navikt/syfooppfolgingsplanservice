package no.nav.syfo.service;

import no.nav.tjeneste.virksomhet.behandlesak.v1.BehandleSakV1;
import no.nav.tjeneste.virksomhet.behandlesak.v1.OpprettSakSakEksistererAllerede;
import no.nav.tjeneste.virksomhet.behandlesak.v1.OpprettSakUgyldigInput;
import no.nav.tjeneste.virksomhet.behandlesak.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.behandlesak.v1.meldinger.WSOpprettSakRequest;
import org.slf4j.Logger;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

public class BehandleSakService {
    private static final Logger LOG = getLogger(BehandleSakService.class);

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
            LOG.error("Sak finnes allerede", e);
            throw new RuntimeException("Sak finnes allerede", e);
        } catch (OpprettSakUgyldigInput e) {
            LOG.error("Ugyldig input", e);
            throw new RuntimeException("Ugyldid input i sak", e);
        }
    }
}
