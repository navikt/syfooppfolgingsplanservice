package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.aktoer.v2.*;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static no.nav.syfo.config.cache.CacheConfig.CACHENAME_AKTOR_FNR;
import static no.nav.syfo.config.cache.CacheConfig.CACHENAME_AKTOR_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class AktoerService implements InitializingBean {

    private static AktoerService instance;

    @Override
    public void afterPropertiesSet() {
        instance = this;
    }

    public static AktoerService aktoerService() {
        return instance;
    }

    private Aktoer_v2PortType aktoerV2;

    @Inject
    public AktoerService(Aktoer_v2PortType aktoerV2) {
        this.aktoerV2 = aktoerV2;
    }

    @Cacheable(value = CACHENAME_AKTOR_ID, key = "#fnr", condition = "#fnr != null")
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

    @Cacheable(value = CACHENAME_AKTOR_FNR, key = "#aktoerId", condition = "#aktoerId != null")
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
