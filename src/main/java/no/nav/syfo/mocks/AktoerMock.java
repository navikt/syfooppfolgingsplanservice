package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.aktoer.v2.*;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.ws.wsconfig.AktoerConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class AktoerMock implements AktoerV2 {

    private static final String MOCK_AKTORID_PREFIX = "10";


    @Override
    public WSHentAktoerIdForIdentListeResponse hentAktoerIdForIdentListe(WSHentAktoerIdForIdentListeRequest request) {
        return null;
    }

    @Override
    public WSHentAktoerIdForIdentResponse hentAktoerIdForIdent(WSHentAktoerIdForIdentRequest request) throws HentAktoerIdForIdentPersonIkkeFunnet {
        return new WSHentAktoerIdForIdentResponse().withAktoerId(mockAktorId(request.getIdent()));
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
        return new WSHentIdentForAktoerIdResponse().withIdent(getFnrFromMockedAktorId(request.getAktoerId()));
    }

    public static String mockAktorId(String fnr) {
        return MOCK_AKTORID_PREFIX.concat(fnr);
    }

    private static String getFnrFromMockedAktorId(String aktorId) {
        return aktorId.replace(MOCK_AKTORID_PREFIX, "");
    }
}
