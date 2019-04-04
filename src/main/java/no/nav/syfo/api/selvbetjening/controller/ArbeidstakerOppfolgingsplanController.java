package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.selvbetjening.domain.RSBrukerOppfolgingsplan;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.OppfoelgingsdialogService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSTAKER;
import static no.nav.syfo.api.selvbetjening.mapper.RSBrukerOppfolgingsplanMapper.oppfolgingsplan2rs;
import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.populerOppfolgingsplanerMedAvbruttPlanListe;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/arbeidstaker/oppfolgingsplaner")
public class ArbeidstakerOppfolgingsplanController {

    private final OIDCRequestContextHolder contextHolder;
    private final OppfoelgingsdialogService oppfoelgingsdialogService;
    private final Metrikk metrikk;

    @Inject
    public ArbeidstakerOppfolgingsplanController(
            OIDCRequestContextHolder contextHolder,
            OppfoelgingsdialogService oppfoelgingsdialogService,
            Metrikk metrikk
    ) {
        this.contextHolder = contextHolder;
        this.oppfoelgingsdialogService = oppfoelgingsdialogService;
        this.metrikk = metrikk;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<RSBrukerOppfolgingsplan> hentArbeidstakersOppfolgingsplaner() {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        List<RSBrukerOppfolgingsplan> liste = mapListe(oppfoelgingsdialogService.hentAktoersOppfoelgingsdialoger(ARBEIDSTAKER, innloggetIdent), oppfolgingsplan2rs);

        metrikk.tellHendelse("hent_oppfolgingsplan_at");

        return populerOppfolgingsplanerMedAvbruttPlanListe(liste);
    }
}
