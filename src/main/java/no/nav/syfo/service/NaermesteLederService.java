package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.config.ws.wsconfig.SyfoOppfoelgingConfig;
import no.nav.syfo.model.Naermesteleder;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentNaermesteLederListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentNaermesteLederSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static no.nav.syfo.mappers.ws.WSNaermesteLederMapper.ws2naermesteLeder;
import static no.nav.syfo.oidc.OIDCUtil.getIssuerToken;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;

@Slf4j
@Service
public class NaermesteLederService {

    private final OIDCRequestContextHolder contextHolder;
    private final SyfoOppfoelgingConfig sykefravaersoppfoelgingConfig;

    @Inject
    public NaermesteLederService(
            OIDCRequestContextHolder contextHolder,
            SyfoOppfoelgingConfig sykefravaersoppfoelgingConfig
    ) {
        this.contextHolder = contextHolder;
        this.sykefravaersoppfoelgingConfig = sykefravaersoppfoelgingConfig;
    }

    public List<Naermesteleder> hentNaermesteLedere(String aktoerId, String oidcIssuer) {
        try {
            WSHentNaermesteLederListeRequest request = new WSHentNaermesteLederListeRequest()
                    .withAktoerId(aktoerId);

            String oidcToken = getIssuerToken(this.contextHolder, oidcIssuer);
            WSHentNaermesteLederListeResponse response = sykefravaersoppfoelgingConfig.hentNaermesteLederListe(request, oidcToken);

            return mapListe(response.getNaermesteLederListe(), ws2naermesteLeder);
        } catch (HentNaermesteLederListeSikkerhetsbegrensning e) {
            log.warn("Fikk sikkerhetsbegrensning ved henting av naermeste ledere for person {}", aktoerId);
            throw new ForbiddenException();
        }
    }

    public Optional<Naermesteleder> hentNaermesteLeder(String aktoerId, String virksomhetsnummer, String oidcIssuer) {
        try {
            WSHentNaermesteLederRequest request = new WSHentNaermesteLederRequest().withAktoerId(aktoerId).withOrgnummer(virksomhetsnummer);

            String oidcToken = getIssuerToken(this.contextHolder, oidcIssuer);
            WSHentNaermesteLederResponse response = sykefravaersoppfoelgingConfig.hentNaermesteLeder(request, oidcToken);

            return of(map(response.getNaermesteLeder(), ws2naermesteLeder));
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
