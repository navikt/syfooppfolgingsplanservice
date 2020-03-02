package no.nav.syfo.api.selvbetjening.controller;

import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.syfo.api.selvbetjening.domain.*;
import no.nav.syfo.domain.Arbeidsoppgave;
import no.nav.syfo.domain.Tiltak;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.service.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Optional;

import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSGIVER;
import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSTAKER;
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

    final static String METRIC_SHARE_WITH_NAV_AT_APPROVAL = "del_plan_med_nav_ved_godkjenning";

    private final Metrikk metrikk;
    private final OIDCRequestContextHolder contextHolder;
    private final ArbeidsoppgaveService arbeidsoppgaveService;
    private final GodkjenningService godkjenningService;
    private final OppfolgingsplanService oppfolgingsplanService;
    private final SamtykkeService samtykkeService;
    private final TiltakService tiltakService;

    @Inject
    public OppfolgingsplanController(
            Metrikk metrikk,
            OIDCRequestContextHolder contextHolder,
            ArbeidsoppgaveService arbeidsoppgaveService,
            GodkjenningService godkjenningService,
            OppfolgingsplanService oppfolgingsplanService,
            SamtykkeService samtykkeService,
            TiltakService tiltakService
    ) {
        this.metrikk = metrikk;
        this.contextHolder = contextHolder;
        this.arbeidsoppgaveService = arbeidsoppgaveService;
        this.godkjenningService = godkjenningService;
        this.oppfolgingsplanService = oppfolgingsplanService;
        this.samtykkeService = samtykkeService;
        this.tiltakService = tiltakService;
    }

    @PostMapping(path = "/avbryt")
    public void avbryt(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        oppfolgingsplanService.avbrytPlan(id, innloggetIdent);

        metrikk.tellHendelse("avbryt_plan");
    }

    @PostMapping(path = "/avvis")
    public void avvis(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        godkjenningService.avvisGodkjenning(id, innloggetIdent);

        metrikk.tellHendelse("avvis_plan");
    }

    @PostMapping(path = "/delmedfastlege")
    public void delMedFastlege(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        oppfolgingsplanService.delMedFastlege(id, innloggetIdent);

        metrikk.tellHendelse("del_plan_med_fastlege");
    }

    @PostMapping(path = "/delmednav")
    public void delMedNav(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        oppfolgingsplanService.delMedNav(id, innloggetIdent);

        metrikk.tellHendelse("del_plan_med_nav");
    }

    @PostMapping(path = "/godkjenn", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public RSGyldighetstidspunkt godkjenn(
            @PathVariable("id") Long id,
            @RequestBody RSGyldighetstidspunkt rsGyldighetstidspunkt,
            @RequestParam("status") String status,
            @RequestParam("aktoer") String aktor,
            @RequestParam(value = "delmednav", required = false) Boolean delMedNav
    ) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        boolean isPlanSharedWithNAV = Optional.ofNullable(delMedNav).orElse(false);
        if (isPlanSharedWithNAV) {
            countShareWithNAVAtApproval();
        }

        godkjenningService.godkjennOppfolgingsplan(id, rsGyldighetstidspunkt, innloggetIdent, "tvungenGodkjenning".equals(status), isPlanSharedWithNAV);

        metrikk.tellHendelse("godkjenn_plan");

        if (rsGyldighetstidspunkt != null) {
            return rsGyldighetstidspunkt;
        }

        return hentGyldighetstidspunktForPlan(id, aktor, innloggetIdent);
    }

    @PostMapping(path = "/godkjennsist", produces = APPLICATION_JSON_VALUE)
    public RSGyldighetstidspunkt godkjenn(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam("aktoer") String aktor,
            @RequestParam(value = "delmednav", required = false) Boolean delMedNav
    ) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        boolean isPlanSharedWithNAV = Optional.ofNullable(delMedNav).orElse(false);
        if (isPlanSharedWithNAV) {
            countShareWithNAVAtApproval();
        }

        godkjenningService.godkjennOppfolgingsplan(id, null, innloggetIdent, "tvungengodkjenning".equals(status), isPlanSharedWithNAV);

        metrikk.tellHendelse("godkjenn_plan_svar");

        return hentGyldighetstidspunktForPlan(id, aktor, innloggetIdent);
    }

    private RSGyldighetstidspunkt hentGyldighetstidspunktForPlan(@PathVariable("id") Long id, @RequestParam("aktoer") String aktor, String innloggetIdent) {
        if ("arbeidsgiver".equals(aktor)) {
            return oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(id, ARBEIDSGIVER, innloggetIdent);
        } else {
            return oppfolgingsplanService.hentGyldighetstidspunktForGodkjentPlan(id, ARBEIDSTAKER, innloggetIdent);
        }
    }

    @PostMapping(path = "/kopier", produces = APPLICATION_JSON_VALUE)
    public long kopier(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        long nyPlanId = oppfolgingsplanService.kopierOppfoelgingsdialog(id, innloggetIdent);

        metrikk.tellHendelse("kopier_plan");

        return nyPlanId;
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

        oppfolgingsplanService.foresporRevidering(id, innloggetIdent);

        metrikk.tellHendelse("forespor_revidering");
    }

    @PostMapping(path = "/nullstillGodkjenning")
    public void nullstillGodkjenning(@PathVariable("id") Long id) {
        String innloggetIdent = getSubjectEksternMedThrows(contextHolder);

        oppfolgingsplanService.nullstillGodkjenning(id, innloggetIdent);

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

        oppfolgingsplanService.oppdaterSistInnlogget(id, innloggetIdent);

        metrikk.tellHendelse("sett_plan");
    }

    private void countShareWithNAVAtApproval() {
        metrikk.tellHendelse(METRIC_SHARE_WITH_NAV_AT_APPROVAL);
    }
}
