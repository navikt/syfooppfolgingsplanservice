package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.selvbetjening.domain.RSBrukerOppfolgingsplan;
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.OppfolgingsplanService;
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
    private final OppfolgingsplanService oppfolgingsplanService;
    private final Metrikk metrikk;

    @Inject
    public ArbeidstakerOppfolgingsplanController(
            OIDCRequestContextHolder contextHolder,
            OppfolgingsplanService oppfolgingsplanService,
            Metrikk metrikk
    ) {
        this.contextHolder = contextHolder;
        this.oppfolgingsplanService = oppfolgingsplanService;
        this.metrikk = metrikk;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<RSBrukerOppfolgingsplan> hentArbeidstakersOppfolgingsplaner() {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        List<RSBrukerOppfolgingsplan> liste = mapListe(oppfolgingsplanService.hentAktorsOppfolgingsplaner(ARBEIDSTAKER, innloggetIdent), oppfolgingsplan2rs);

        metrikk.tellHendelse("hent_oppfolgingsplan_at");

        return populerOppfolgingsplanerMedAvbruttPlanListe(liste);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Long opprettOppfolgingsplanSomArbeidstaker(@RequestBody RSOpprettOppfoelgingsdialog rsOpprettOppfoelgingsdialog) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        Long id = oppfolgingsplanService.opprettOppfolgingsplan(rsOpprettOppfoelgingsdialog.sykmeldtFnr(innloggetIdent), innloggetIdent);

        metrikk.tellHendelse("opprett_oppfolgingsplan_at");

        return id;
    }
}
