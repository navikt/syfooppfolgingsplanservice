package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.*;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.ws.wsconfig.ArbeidsfordelingConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class ArbeidsfordelingMock implements ArbeidsfordelingV1 {
    @Override
    public WSFinnAlleBehandlendeEnheterListeResponse finnAlleBehandlendeEnheterListe(WSFinnAlleBehandlendeEnheterListeRequest wsFinnAlleBehandlendeEnheterListeRequest) throws FinnAlleBehandlendeEnheterListeUgyldigInput {
        return null;
    }

    @Override
    public WSFinnBehandlendeEnhetListeResponse finnBehandlendeEnhetListe(WSFinnBehandlendeEnhetListeRequest wsFinnBehandlendeEnhetListeRequest) throws FinnBehandlendeEnhetListeUgyldigInput {
        return null;
    }

    @Override
    public void ping() {

    }
}
