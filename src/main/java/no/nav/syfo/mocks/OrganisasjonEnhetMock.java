package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.FinnNAVKontorUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.HentOverordnetEnhetListeEnhetIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.*;

public class OrganisasjonEnhetMock implements OrganisasjonEnhetV2 {
    @Override
    public WSHentFullstendigEnhetListeResponse hentFullstendigEnhetListe(WSHentFullstendigEnhetListeRequest wsHentFullstendigEnhetListeRequest) {
        return null;
    }

    @Override
    public WSHentOverordnetEnhetListeResponse hentOverordnetEnhetListe(WSHentOverordnetEnhetListeRequest wsHentOverordnetEnhetListeRequest) throws HentOverordnetEnhetListeEnhetIkkeFunnet {
        return null;
    }

    @Override
    public WSFinnNAVKontorResponse finnNAVKontor(WSFinnNAVKontorRequest wsFinnNAVKontorRequest) throws FinnNAVKontorUgyldigInput {
        return null;
    }

    @Override
    public WSHentEnhetBolkResponse hentEnhetBolk(WSHentEnhetBolkRequest wsHentEnhetBolkRequest) {
        return null;
    }

    @Override
    public void ping() {

    }
}
