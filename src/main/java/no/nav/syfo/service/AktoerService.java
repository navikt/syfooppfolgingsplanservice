package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.aktoer.v2.*;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class AktoerService {

    private AktoerV2 aktoerV2;

    @Inject
    public AktoerService(AktoerV2 aktoerV2) {
        this.aktoerV2 = aktoerV2;
    }

    @Cacheable("aktoer")
    public String hentAktoerIdForFnr(String fnr) {
        if (isBlank(fnr) || !fnr.matches("\\d{11}$")) {
            log.error("Prøvde å hente aktoerId");
            throw new RuntimeException();
        }

        try {
            return aktoerV2.hentAktoerIdForIdent(
                    new WSHentAktoerIdForIdentRequest()
                            .withIdent(fnr)
            ).getAktoerId();
        } catch (HentAktoerIdForIdentPersonIkkeFunnet e) {
            log.warn("AktoerID ikke funnet for fødselsnummer!", e);
            throw new RuntimeException();
        }
    }

    @Cacheable("aktoer")
    public String hentFnrForAktoer(String aktoerId) {
        if (isBlank(aktoerId) || !aktoerId.matches("\\d{13}$")) {
            log.error("Prøvde å hente fnr med aktoerId {}", aktoerId);
            throw new RuntimeException();
        }

        try {
            return aktoerV2.hentIdentForAktoerId(
                    new WSHentIdentForAktoerIdRequest()
                            .withAktoerId(aktoerId)
            ).getIdent();
        } catch (HentIdentForAktoerIdPersonIkkeFunnet e) {
            log.warn("FNR ikke funnet for aktoerId!", e);
            throw new RuntimeException();
        }
    }
}
