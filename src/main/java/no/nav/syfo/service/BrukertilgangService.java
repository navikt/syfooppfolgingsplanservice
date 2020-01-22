package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrukertilgangService {

    private AktorregisterConsumer aktorregisterConsumer;

    private NarmesteLederConsumer narmesteLederConsumer;

    @Autowired
    public BrukertilgangService(
            AktorregisterConsumer aktorregisterConsumer,
            NarmesteLederConsumer narmesteLederConsumer
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.narmesteLederConsumer = narmesteLederConsumer;
    }

    public boolean sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(String innloggetIdent, String oppslaattFnr) {
        return !(sporInnloggetBrukerOmSegSelv(innloggetIdent, oppslaattFnr) || sporInnloggetBrukerOmEnAnsatt(innloggetIdent, oppslaattFnr));
    }

    private boolean sporInnloggetBrukerOmEnAnsatt(String innloggetIdent, String oppslaattFnr) {
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetIdent);
        String oppslaattAktoerId = aktorregisterConsumer.hentAktorIdForFnr(oppslaattFnr);
        return narmesteLederConsumer.ansatte(innloggetAktoerId)
                .stream()
                .map(Ansatt::aktoerId)
                .anyMatch(oppslaattAktoerId::equals);
    }

    private boolean sporInnloggetBrukerOmSegSelv(String innloggetIdent, String brukerFnr) {
        return brukerFnr.equals(innloggetIdent);
    }
}
