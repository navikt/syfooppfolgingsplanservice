package no.nav.syfo.service;

import no.nav.syfo.brukertilgang.BrukertilgangConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BrukertilgangService {

    private BrukertilgangConsumer brukertilgangConsumer;

    @Autowired
    public BrukertilgangService(
            BrukertilgangConsumer brukertilgangConsumer
    ) {
        this.brukertilgangConsumer = brukertilgangConsumer;
    }

    @Cacheable(cacheNames = "tilgangtilident", key = "#innloggetIdent.concat(#oppslaattFnr)", condition = "#innloggetIdent != null && #oppslaattFnr != null")
    public boolean tilgangTilOppslattIdent(String innloggetIdent, String oppslaattFnr) {
        return oppslaattFnr.equals(innloggetIdent) || brukertilgangConsumer.hasAccessToAnsatt(oppslaattFnr);
    }
}
