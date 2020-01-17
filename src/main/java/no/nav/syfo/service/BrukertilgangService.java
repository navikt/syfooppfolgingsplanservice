package no.nav.syfo.service;

import no.nav.syfo.model.Ansatt;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrukertilgangService {

    private AktoerService aktoerService;

    private NarmesteLederConsumer narmesteLederConsumer;

    @Autowired
    public BrukertilgangService(
            AktoerService aktoerService,
            NarmesteLederConsumer narmesteLederConsumer
    ) {
        this.aktoerService = aktoerService;
        this.narmesteLederConsumer = narmesteLederConsumer;
    }

    public boolean sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(String innloggetIdent, String oppslaattFnr) {
        return !(sporInnloggetBrukerOmSegSelv(innloggetIdent, oppslaattFnr) || sporInnloggetBrukerOmEnAnsatt(innloggetIdent, oppslaattFnr));
    }

    private boolean sporInnloggetBrukerOmEnAnsatt(String innloggetIdent, String oppslaattFnr) {
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(innloggetIdent);
        String oppslaattAktoerId = aktoerService.hentAktoerIdForFnr(oppslaattFnr);
        return narmesteLederConsumer.ansatte(innloggetAktoerId)
                .stream()
                .map(Ansatt::aktoerId)
                .anyMatch(oppslaattAktoerId::equals);
    }

    private boolean sporInnloggetBrukerOmSegSelv(String innloggetIdent, String brukerFnr) {
        return brukerFnr.equals(innloggetIdent);
    }
}
