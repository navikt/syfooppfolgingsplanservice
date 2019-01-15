package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnAlleBehandlendeEnheterListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnAlleBehandlendeEnheterListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnAlleBehandlendeEnheterListeResponse;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeResponse;

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
