package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.sak.v1.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.FinnSakUgyldigInput;
import no.nav.tjeneste.virksomhet.sak.v1.HentSakSakIkkeFunnet;
import no.nav.tjeneste.virksomhet.sak.v1.SakV1;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.WSFinnSakRequest;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.WSFinnSakResponse;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.WSHentSakRequest;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.WSHentSakResponse;

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
