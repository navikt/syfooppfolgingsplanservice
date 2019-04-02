package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.selvbetjening.domain.RSArbeidsoppgave;
import no.nav.syfo.domain.Arbeidsoppgave;
import no.nav.syfo.service.ArbeidsoppgaveService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.api.selvbetjening.mapper.RSArbeidsoppgaveMapper.rs2arbeidsoppgave;
import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static no.nav.syfo.util.MapUtil.map;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/oppfolgingsplan/actions/{id}")
public class OppfolgingsplanController {

    private final OIDCRequestContextHolder contextHolder;
    private final ArbeidsoppgaveService arbeidsoppgaveService;

    @Inject
    public OppfolgingsplanController(
            OIDCRequestContextHolder contextHolder,
            ArbeidsoppgaveService arbeidsoppgaveService
    ) {
        this.contextHolder = contextHolder;
        this.arbeidsoppgaveService = arbeidsoppgaveService;
    }

    @PostMapping(path = "/lagreArbeidsoppgave", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public long lagreArbeidsoppgave(
            @PathVariable("id") Long id,
            @RequestBody RSArbeidsoppgave rsArbeidsoppgave) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        Arbeidsoppgave arbeidsoppgave = map(rsArbeidsoppgave, rs2arbeidsoppgave);

        return arbeidsoppgaveService.lagreArbeidsoppgave(id, arbeidsoppgave, innloggetIdent);
    }
}
