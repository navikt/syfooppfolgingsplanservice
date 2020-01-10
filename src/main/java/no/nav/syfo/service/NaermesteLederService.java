package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.config.ws.wsconfig.SyfoOppfoelgingConfig;
import no.nav.syfo.model.Naermesteleder;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.HentNaermesteLederListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.WSHentNaermesteLederListeRequest;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.WSHentNaermesteLederListeResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.List;

import static no.nav.syfo.mappers.ws.WSNaermesteLederMapper.ws2naermesteLeder;
import static no.nav.syfo.oidc.OIDCUtil.getIssuerToken;
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
}
