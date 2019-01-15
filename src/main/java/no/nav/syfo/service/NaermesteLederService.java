package no.nav.syfo.service;

import no.nav.syfo.model.Ansatt;
import no.nav.syfo.model.Naermesteleder;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentNaermesteLederListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentNaermesteLederSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentNaermesteLedersAnsattListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.SykefravaersoppfoelgingV1;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.*;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static no.nav.syfo.mappers.ws.WSAnsattMapper.ws2ansatt;
import static no.nav.syfo.mappers.ws.WSNaermesteLederMapper.ws2naermesteLeder;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;
import static org.slf4j.LoggerFactory.getLogger;

public class NaermesteLederService {
    private static final Logger LOG = getLogger(NaermesteLederService.class);

    @Inject
    private SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1;

    @Cacheable(value = "syfo", keyGenerator = "userkeygenerator")
    public List<Ansatt> hentAnsatte(String aktoerId) {
        try {
            WSHentNaermesteLedersAnsattListeResponse response = sykefravaersoppfoelgingV1.hentNaermesteLedersAnsattListe(new WSHentNaermesteLedersAnsattListeRequest()
                    .withAktoerId(aktoerId));
            return mapListe(response.getAnsattListe(), ws2ansatt);
        } catch (HentNaermesteLedersAnsattListeSikkerhetsbegrensning e) {
            LOG.warn("Fikk sikkerhetsbegrensning ved henting av ansatte for person {}", aktoerId);
            throw new ForbiddenException();
        }
    }

    @Cacheable(value = "syfo", keyGenerator = "userkeygenerator")
    public List<Naermesteleder> hentNaermesteLedere(String aktoerId) {
        try {
            WSHentNaermesteLederListeResponse response = sykefravaersoppfoelgingV1.hentNaermesteLederListe(new WSHentNaermesteLederListeRequest()
                    .withAktoerId(aktoerId));
            return mapListe(response.getNaermesteLederListe(), ws2naermesteLeder);
        } catch (HentNaermesteLederListeSikkerhetsbegrensning e) {
            LOG.warn("Fikk sikkerhetsbegrensning ved henting av naermeste ledere for person {}", aktoerId);
            throw new ForbiddenException();
        }
    }

    @Cacheable(value = "syfo", keyGenerator = "userkeygenerator")
    public Optional<Naermesteleder> hentNaermesteLeder(String aktoerId, String virksomhetsnummer) {
        try {
            return of(map(sykefravaersoppfoelgingV1.hentNaermesteLeder(new WSHentNaermesteLederRequest().withAktoerId(aktoerId).withOrgnummer(virksomhetsnummer)).getNaermesteLeder(), ws2naermesteLeder));
        } catch (HentNaermesteLederSikkerhetsbegrensning e) {
            LOG.warn("Fikk sikkerhetsbegrensning ved henting av naermeste leder for person {} i virksomhet {}", aktoerId, virksomhetsnummer);
            throw new ForbiddenException();
        } catch (RuntimeException e) {
            LOG.error("Runtime-feil mot SyfoService. Trolig restart av syfoservice eller at datapower er nede e.l. " +
                    "Parametere: aktoerId: {} virksomhetsnummer: {} av bruker ", aktoerId, virksomhetsnummer, e);
            throw e;
        }
    }
}
