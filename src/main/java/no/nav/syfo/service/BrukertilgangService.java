package no.nav.syfo.service;

import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getIssuerToken;

import no.nav.syfo.brukertilgang.BrukertilgangConsumer;

@Service
public class BrukertilgangService {

    private BrukertilgangConsumer brukertilgangConsumer;
    private TokenValidationContextHolder contextHolder;

    @Autowired
    public BrukertilgangService(
            BrukertilgangConsumer brukertilgangConsumer,
            TokenValidationContextHolder contextHolder
    ) {
        this.brukertilgangConsumer = brukertilgangConsumer;
        this.contextHolder = contextHolder;
    }

    @Cacheable(cacheNames = "tilgangtilident", key = "#innloggetIdent.concat(#oppslaattFnr)", condition = "#innloggetIdent != null && #oppslaattFnr != null")
    public boolean tilgangTilOppslattIdent(String innloggetIdent, String oppslaattFnr) {
        return oppslaattFnr.equals(innloggetIdent) || brukertilgangConsumer.hasAccessToAnsatt(oppslaattFnr, getIssuerToken(contextHolder, EKSTERN));
    }
}
