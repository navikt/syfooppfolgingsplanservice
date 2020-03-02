package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.KommentarService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/kommentar/actions/{id}")
public class KommentarController {

    private final OIDCRequestContextHolder contextHolder;
    private final KommentarService kommentarService;
    private final Metrikk metrikk;

    @Inject
    public KommentarController(
            OIDCRequestContextHolder contextHolder,
            KommentarService kommentarService,
            Metrikk metrikk
    ) {
        this.contextHolder = contextHolder;
        this.kommentarService = kommentarService;
        this.metrikk = metrikk;
    }

    @PostMapping(path = "/slett")
    public void slettKommentar(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        kommentarService.slettKommentar(id, innloggetIdent);

        metrikk.tellHendelse("slett_kommentar");
    }
}
