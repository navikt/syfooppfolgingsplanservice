package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.*;

public class AktoerMock implements AktoerV2 {


    @Override
    public WSHentAktoerIdForIdentListeResponse hentAktoerIdForIdentListe(WSHentAktoerIdForIdentListeRequest request) {
        return null;
    }

    @Override
    public WSHentAktoerIdForIdentResponse hentAktoerIdForIdent(WSHentAktoerIdForIdentRequest request) throws HentAktoerIdForIdentPersonIkkeFunnet {
        return new WSHentAktoerIdForIdentResponse().withAktoerId("10101010101");
    }

    @Override
    public WSHentIdentForAktoerIdListeResponse hentIdentForAktoerIdListe(WSHentIdentForAktoerIdListeRequest request) {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public WSHentIdentForAktoerIdResponse hentIdentForAktoerId(WSHentIdentForAktoerIdRequest request) throws HentIdentForAktoerIdPersonIkkeFunnet {
        return new WSHentIdentForAktoerIdResponse().withIdent("1010101010101");
    }
}
