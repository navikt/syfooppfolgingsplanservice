package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.brukerprofil.v3.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.meldinger.WSHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.meldinger.WSHentKontaktinformasjonOgPreferanserResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.ws.wsconfig.TpsConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class BrukerprofilMock implements BrukerprofilV3 {

    public final static String PERSON_FORNAVN = "Fornavn";
    public final static String PERSON_ETTERNAVN = "Etternavn";

    @Override
    public void ping() {

    }

    @Override
    public WSHentKontaktinformasjonOgPreferanserResponse hentKontaktinformasjonOgPreferanser(WSHentKontaktinformasjonOgPreferanserRequest wsHentKontaktinformasjonOgPreferanserRequest) throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt {
        return new WSHentKontaktinformasjonOgPreferanserResponse()
                .withBruker(new WSBruker()
                        .withDiskresjonskode(new WSDiskresjonskoder()
                                .withValue("0")
                        )
                        .withPersonnavn(new WSPersonnavn()
                                .withFornavn(PERSON_FORNAVN)
                                .withEtternavn(PERSON_ETTERNAVN)
                        )
                );
    }
}
