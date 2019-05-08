package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.selvbetjening.domain.RSBrukerOppfolgingsplan;
import no.nav.syfo.api.selvbetjening.domain.RSOpprettOppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.util.List;

import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSGIVER;
import static no.nav.syfo.api.selvbetjening.mapper.RSBrukerOppfolgingsplanMapper.oppfolgingsplan2rs;
import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.populerOppfolgingsplanerMedAvbruttPlanListe;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/arbeidsgiver/oppfolgingsplaner")
public class ArbeidsgiverOppfolgingsplanController {

    private final OIDCRequestContextHolder contextHolder;
    private final AktoerService aktorService;
    private final NaermesteLederService naermesteLederService;
    private final OppfoelgingsdialogService oppfoelgingsdialogService;
    private final Metrikk metrikk;

    @Inject
    public ArbeidsgiverOppfolgingsplanController(
            OIDCRequestContextHolder contextHolder,
            AktoerService aktorService,
            NaermesteLederService naermesteLederService,
            OppfoelgingsdialogService oppfoelgingsdialogService,
            Metrikk metrikk
    ) {
        this.contextHolder = contextHolder;
        this.aktorService = aktorService;
        this.naermesteLederService = naermesteLederService;
        this.oppfoelgingsdialogService = oppfoelgingsdialogService;
        this.metrikk = metrikk;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<RSBrukerOppfolgingsplan> hentArbeidsgiversOppfolgingsplaner() {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        List<RSBrukerOppfolgingsplan> liste = mapListe(oppfoelgingsdialogService.hentAktoersOppfoelgingsdialoger(ARBEIDSGIVER, innloggetIdent), oppfolgingsplan2rs);

        metrikk.tellHendelse("hent_oppfolgingsplan_ag");

        return populerOppfolgingsplanerMedAvbruttPlanListe(liste);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Long opprettOppfolgingsplanSomArbeidsgiver(@RequestBody RSOpprettOppfoelgingsdialog rsOpprettOppfolgingsplan) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        String innloggetAktorId = aktorService.hentAktoerIdForFnr(innloggetIdent);
        String sykmeldtAktorId = aktorService.hentAktoerIdForFnr(rsOpprettOppfolgingsplan.sykmeldtFnr);

        if (naermesteLederService.erAktorLederForAktor(innloggetAktorId, sykmeldtAktorId, EKSTERN)) {
            Long id = oppfoelgingsdialogService.opprettOppfoelgingsdialog(rsOpprettOppfolgingsplan, innloggetIdent);

            metrikk.tellHendelse("opprett_oppfolgingsplan_ag");

            return id;
        } else {
            throw new ForbiddenException();
        }
    }
}
