package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v3.meldinger.WSHentKontaktinformasjonOgPreferanserRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class BrukerprofilService {

    private static final Logger log = getLogger(BrukerprofilService.class);

    private BrukerprofilV3 brukerprofilV3;
    private AktorregisterConsumer aktorregisterConsumer;
    private final PdlConsumer pdlConsumer;

    @Inject
    public BrukerprofilService(
            BrukerprofilV3 brukerprofilV3,
            AktorregisterConsumer aktorregisterConsumer,
            PdlConsumer pdlConsumer
    ) {
        this.brukerprofilV3 = brukerprofilV3;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.pdlConsumer = pdlConsumer;
    }

    public String hentNavnByAktoerId(String aktoerId) {
        if (!aktoerId.matches("\\d{13}$")) {
            throw new RuntimeException();
        }
        String fnr = aktorregisterConsumer.hentFnrForAktor(aktoerId);
        return pdlConsumer.person(fnr).getName();
    }

    public boolean erKode6eller7(String fnr) {
        if (!fnr.matches("\\d{11}$")) {
            throw new RuntimeException();
        }
        try {
            WSPerson wsPerson = brukerprofilV3.hentKontaktinformasjonOgPreferanser(new WSHentKontaktinformasjonOgPreferanserRequest()
                    .withIdent(new WSNorskIdent()
                            .withIdent(fnr)
                            .withType(new WSPersonidenter()
                                    .withKodeRef("http://nav.no/kodeverk/Term/Personidenter/FNR/nb/F_c3_b8dselnummer?v=1")
                                    .withValue("FNR")
                            ))).getBruker();
            return ((wsPerson.getDiskresjonskode() != null) && "6".equals(wsPerson.getDiskresjonskode().getValue())) || ((wsPerson.getDiskresjonskode() != null) && "7".equals(wsPerson.getDiskresjonskode().getValue()));
        } catch (HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt e) {
            log.error("HentKontaktinformasjonOgPreferanserPersonIdentErUtgaatt ved oppslag mot TPS", e);
            throw new RuntimeException();
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            log.error("Sikkerhetsbegrensning ved henting av person fra TPS", e);
            throw new ForbiddenException();
        } catch (HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            log.error("HentKontaktinformasjonOgPreferanserPersonIkkeFunnet ved oppslag mot TPS", e);
            return true;
        } catch (RuntimeException e) {
            log.error("Fikk RuntimeException ved oppslag mot TPS", e);
            throw e;
        }
    }

}
