package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.sak.v1.*;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.ws.wsconfig.SakConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class SakV1Mock implements SakV1 {
    @Override
    public WSFinnSakResponse finnSak(WSFinnSakRequest request) throws FinnSakForMangeForekomster, FinnSakUgyldigInput {
        return null;
    }

    @Override
    public WSHentSakResponse hentSak(WSHentSakRequest request) throws HentSakSakIkkeFunnet {
        return null;
    }

    @Override
    public void ping() {

    }
}
