package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.person.v3.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.ws.wsconfig.PersonConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class PersonMock implements PersonV3 {
    @Override
    public WSHentPersonResponse hentPerson(WSHentPersonRequest wsHentPersonRequest) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        return null;
    }

    @Override
    public WSHentGeografiskTilknytningResponse hentGeografiskTilknytning(WSHentGeografiskTilknytningRequest wsHentGeografiskTilknytningRequest) throws HentGeografiskTilknytningSikkerhetsbegrensing, HentGeografiskTilknytningPersonIkkeFunnet {
        return null;
    }

    @Override
    public WSHentSikkerhetstiltakResponse hentSikkerhetstiltak(WSHentSikkerhetstiltakRequest wsHentSikkerhetstiltakRequest) throws HentSikkerhetstiltakPersonIkkeFunnet {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public WSHentPersonnavnBolkResponse hentPersonnavnBolk(WSHentPersonnavnBolkRequest wsHentPersonnavnBolkRequest) {
        return null;
    }
}
