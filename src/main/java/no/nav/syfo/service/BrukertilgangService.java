package no.nav.syfo.service;

import no.nav.syfo.model.Ansatt;
import no.nav.syfo.oidc.OIDCIssuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrukertilgangService {

    private AktoerService aktoerService;

    private NaermesteLederService naermesteLederService;

    @Autowired
    public BrukertilgangService(
            AktoerService aktoerService,
            NaermesteLederService naermesteLederService
    ) {
        this.aktoerService = aktoerService;
        this.naermesteLederService = naermesteLederService;
    }

    public boolean sporOmNoenAndreEnnSegSelvEllerEgneAnsatte(String innloggetIdent, String oppslaattFnr) {
        return !(sporInnloggetBrukerOmSegSelv(innloggetIdent, oppslaattFnr) || sporInnloggetBrukerOmEnAnsatt(innloggetIdent, oppslaattFnr));
    }

    private boolean sporInnloggetBrukerOmEnAnsatt(String innloggetIdent, String oppslaattFnr) {
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(innloggetIdent);
        String oppslaattAktoerId = aktoerService.hentAktoerIdForFnr(oppslaattFnr);
        return naermesteLederService.hentAnsatte(innloggetAktoerId, OIDCIssuer.EKSTERN)
                .stream()
                .map(Ansatt::aktoerId)
                .anyMatch(oppslaattAktoerId::equals);
    }

    private boolean sporInnloggetBrukerOmSegSelv(String innloggetIdent, String brukerFnr) {
        return brukerFnr.equals(innloggetIdent);
    }
}
