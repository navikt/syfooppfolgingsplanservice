package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.model.Naermesteleder;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.*;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static no.nav.syfo.mappers.ws.WSAnsattMapper.ws2ansatt;
import static no.nav.syfo.mappers.ws.WSNaermesteLederMapper.ws2naermesteLeder;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;

@Slf4j
@Service
public class NaermesteLederService {

    private SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1;

    @Inject
    public NaermesteLederService(SykefravaersoppfoelgingV1 sykefravaersoppfoelgingV1) {
        this.sykefravaersoppfoelgingV1 = sykefravaersoppfoelgingV1;
    }

    public List<Ansatt> hentAnsatte(String aktoerId) {
        try {
            WSHentNaermesteLedersAnsattListeResponse response = sykefravaersoppfoelgingV1.hentNaermesteLedersAnsattListe(new WSHentNaermesteLedersAnsattListeRequest()
                    .withAktoerId(aktoerId));
            return mapListe(response.getAnsattListe(), ws2ansatt);
        } catch (HentNaermesteLedersAnsattListeSikkerhetsbegrensning e) {
            log.warn("Fikk sikkerhetsbegrensning ved henting av ansatte for person {}", aktoerId);
            throw new ForbiddenException();
        }
    }

    public List<Naermesteleder> hentNaermesteLedere(String aktoerId) {
        try {
            WSHentNaermesteLederListeResponse response = sykefravaersoppfoelgingV1.hentNaermesteLederListe(new WSHentNaermesteLederListeRequest()
                    .withAktoerId(aktoerId));
            return mapListe(response.getNaermesteLederListe(), ws2naermesteLeder);
        } catch (HentNaermesteLederListeSikkerhetsbegrensning e) {
            log.warn("Fikk sikkerhetsbegrensning ved henting av naermeste ledere for person {}", aktoerId);
            throw new ForbiddenException();
        }
    }

    public Optional<Naermesteleder> hentNaermesteLeder(String aktoerId, String virksomhetsnummer) {
        try {
            return of(map(sykefravaersoppfoelgingV1.hentNaermesteLeder(new WSHentNaermesteLederRequest().withAktoerId(aktoerId).withOrgnummer(virksomhetsnummer)).getNaermesteLeder(), ws2naermesteLeder));
        } catch (HentNaermesteLederSikkerhetsbegrensning e) {
            log.warn("Fikk sikkerhetsbegrensning ved henting av naermeste leder for person {} i virksomhet {}", aktoerId, virksomhetsnummer);
            throw new ForbiddenException();
        } catch (RuntimeException e) {
            log.error("Runtime-feil mot SyfoService. Trolig restart av syfoservice eller at datapower er nede e.l. " +
                    "Parametere: aktoerId: {} virksomhetsnummer: {} av bruker ", aktoerId, virksomhetsnummer, e);
            throw e;
        }
    }
}
