package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.behandlesak.v1.BehandleSakV1;
import no.nav.tjeneste.virksomhet.behandlesak.v1.OpprettSakSakEksistererAllerede;
import no.nav.tjeneste.virksomhet.behandlesak.v1.OpprettSakUgyldigInput;
import no.nav.tjeneste.virksomhet.behandlesak.v1.meldinger.WSOpprettSakRequest;
import no.nav.tjeneste.virksomhet.behandlesak.v1.meldinger.WSOpprettSakResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.ws.wsconfig.BehandleSakConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class BehandleSakMock implements BehandleSakV1 {
    @Override
    public void ping() {

    }

    @Override
    public WSOpprettSakResponse opprettSak(WSOpprettSakRequest wsOpprettSakRequest) throws OpprettSakSakEksistererAllerede, OpprettSakUgyldigInput {
        return null;
    }
}
