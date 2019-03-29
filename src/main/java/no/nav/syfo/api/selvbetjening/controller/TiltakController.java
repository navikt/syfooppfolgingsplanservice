package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.ArbeidsoppgaveService;
import no.nav.syfo.service.TiltakService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/tiltak/actions/{id}")
public class TiltakController {

    private final OIDCRequestContextHolder contextHolder;
    private final TiltakService tiltakService;
    private final Metrikk metrikk;

    @Inject
    public TiltakController(
            OIDCRequestContextHolder contextHolder,
            TiltakService tiltakService,
            Metrikk metrikk
    ) {
        this.contextHolder = contextHolder;
        this.tiltakService = tiltakService;
        this.metrikk = metrikk;
    }

    @PostMapping(path = "/slett")
    public void slettTiltak(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        tiltakService.slettTiltak(id, innloggetIdent);

        metrikk.tellHendelse("slett_tiltak");
    }
}
