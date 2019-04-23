package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.security.spring.oidc.validation.api.ProtectedWithClaims;
import no.nav.syfo.api.selvbetjening.domain.RSArbeidsoppgave;
import no.nav.syfo.api.selvbetjening.domain.RSTiltak;
import no.nav.syfo.domain.Arbeidsoppgave;
import no.nav.syfo.domain.Tiltak;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static no.nav.syfo.api.selvbetjening.mapper.RSArbeidsoppgaveMapper.rs2arbeidsoppgave;
import static no.nav.syfo.api.selvbetjening.mapper.RSTiltakMapper.rs2tiltak;
import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oidc.OIDCUtil.getSubjectEksternMedThrows;
import static no.nav.syfo.util.MapUtil.map;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@ProtectedWithClaims(issuer = EKSTERN)
@RequestMapping(value = "/api/oppfolgingsplan/actions/{id}")
public class OppfolgingsplanController {

    private final Metrikk metrikk;
    private final OIDCRequestContextHolder contextHolder;
    private final ArbeidsoppgaveService arbeidsoppgaveService;
    private final OppfoelgingsdialogService oppfoelgingsdialogService;
    private final SamtykkeService samtykkeService;
    private final TiltakService tiltakService;

    @Inject
    public OppfolgingsplanController(
            Metrikk metrikk,
            OIDCRequestContextHolder contextHolder,
            ArbeidsoppgaveService arbeidsoppgaveService,
            OppfoelgingsdialogService oppfoelgingsdialogService,
            SamtykkeService samtykkeService,
            TiltakService tiltakService
    ) {
        this.metrikk = metrikk;
        this.contextHolder = contextHolder;
        this.arbeidsoppgaveService = arbeidsoppgaveService;
        this.oppfoelgingsdialogService = oppfoelgingsdialogService;
        this.samtykkeService = samtykkeService;
        this.tiltakService = tiltakService;
    }

    @PostMapping(path = "/delmednav")
    public void delMedNav(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        oppfoelgingsdialogService.delMedNav(id, innloggetIdent);

        metrikk.tellHendelse("del_plan_med_nav");
    }

    @PostMapping(path = "/lagreArbeidsoppgave", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public long lagreArbeidsoppgave(
            @PathVariable("id") Long id,
            @RequestBody RSArbeidsoppgave rsArbeidsoppgave) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        Arbeidsoppgave arbeidsoppgave = map(rsArbeidsoppgave, rs2arbeidsoppgave);

        return arbeidsoppgaveService.lagreArbeidsoppgave(id, arbeidsoppgave, innloggetIdent);
    }

    @PostMapping(path = "/lagreTiltak", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public long lagreTiltak(
            @PathVariable("id") Long id,
            @RequestBody RSTiltak rsTiltak) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        Tiltak tiltak = map(rsTiltak, rs2tiltak);

        return tiltakService.lagreTiltak(id, tiltak, innloggetIdent);
    }

    @PostMapping(path = "/foresporRevidering")
    public void foresporRevidering(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        oppfoelgingsdialogService.foresporRevidering(id, innloggetIdent);

        metrikk.tellHendelse("forespor_revidering");
    }

    @PostMapping(path = "/nullstillGodkjenning")
    public void nullstillGodkjenning(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        oppfoelgingsdialogService.nullstillGodkjenning(id, innloggetIdent);

        metrikk.tellHendelse("nullstill_godkjenning");
    }

    @PostMapping(path = "/samtykk")
    public void samtykk(
            @PathVariable("id") Long id,
            @RequestParam("samtykke") Boolean samtykke
    ) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        samtykkeService.giSamtykke(id, innloggetIdent, samtykke);

        metrikk.tellHendelse("samtykk_plan");
    }

    @PostMapping(path = "/sett")
    public void sett(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        oppfoelgingsdialogService.oppdaterSistInnlogget(id, innloggetIdent);

        metrikk.tellHendelse("sett_plan");
    }
}
