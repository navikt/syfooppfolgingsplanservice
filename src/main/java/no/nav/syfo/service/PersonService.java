package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.person.v3.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*;

import javax.inject.Inject;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Slf4j
public class PersonService {

    private static final String KODE6 = "SPSF";
    private static final String KODE7 = "SPFO";

    @Inject
    private PersonV3 personV3;

    public String hentGeografiskTilknytning(String fnr) {
        try {
            return of(personV3.hentGeografiskTilknytning(
                    new WSHentGeografiskTilknytningRequest()
                            .withAktoer(new WSPersonIdent().withIdent(new WSNorskIdent().withIdent(fnr)))))
                    .map(WSHentGeografiskTilknytningResponse::getGeografiskTilknytning)
                    .map(WSGeografiskTilknytning::getGeografiskTilknytning)
                    .orElse(null);
        } catch (HentGeografiskTilknytningSikkerhetsbegrensing | HentGeografiskTilknytningPersonIkkeFunnet e) {
            log.error("Feil ved henting av geografisk tilknytning for fnr {}", fnr, e);
            throw new RuntimeException("Feil ved henting av geografisk tilknytning", e);
        } catch (RuntimeException e) {
            log.error("Feil ved henting av geografisk tilknytning for fnr {}", fnr, e);
            throw e;
        }
    }

    public boolean erDiskresjonsmerket(String fnr) {
        try {
            WSPerson wsPerson = personV3.hentPerson(
                    new WSHentPersonRequest()
                            .withAktoer(new WSPersonIdent()
                                    .withIdent(new WSNorskIdent()
                                            .withIdent(fnr))))
                    .getPerson();
            String diskresjonskode = ofNullable(wsPerson.getDiskresjonskode()).map(WSDiskresjonskoder::getValue).orElse("");
            return KODE6.equals(diskresjonskode) || KODE7.equals(diskresjonskode);
        } catch (HentPersonSikkerhetsbegrensning e) {
            log.error("Feil ved henting av diskresjonskode fra TPS");
            throw new RuntimeException("Feil ved henting av diskresjonskode fra TPS");
        } catch (HentPersonPersonIkkeFunnet e) {
            log.error("Prøvde å hente diskresjonskode fra TPS, men fant ikke fnr");
            throw new RuntimeException("Prøvde å hente diskresjonskode fra TPS, men fant ikke fnr");
        } catch (RuntimeException e) {
            log.error("Kall mot TPS i forbindelse med henting av diskresjonskode feilet");
            throw e;
        }
    }
}
