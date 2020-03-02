package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.ArbeidsoppgaveService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/arbeidsoppgave/actions/{arbeidsoppgaveId}")
public class ArbeidsoppgaveController {

    private final OIDCRequestContextHolder contextHolder;
    private final ArbeidsoppgaveService arbeidsoppgaveService;
    private final Metrikk metrikk;

    @Inject
    public ArbeidsoppgaveController(
            OIDCRequestContextHolder contextHolder,
            ArbeidsoppgaveService arbeidsoppgaveService,
            Metrikk metrikk
    ) {
        this.contextHolder = contextHolder;
        this.arbeidsoppgaveService = arbeidsoppgaveService;
        this.metrikk = metrikk;
    }

    @PostMapping(path = "/slett")
    public void slettArbeidsoppgave(@PathVariable("arbeidsoppgaveId") Long arbeidsoppgaveId) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        arbeidsoppgaveService.slettArbeidsoppgave(arbeidsoppgaveId, innloggetIdent);

        metrikk.tellHendelse("slett_arbeidsoppgave");
    }
}
