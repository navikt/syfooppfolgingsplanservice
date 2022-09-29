package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.api.selvbetjening.domain.*;
import no.nav.syfo.dialogmelding.DialogmeldingService;
import no.nav.syfo.domain.*;
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.util.ConflictException;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSGIVER;
import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSTAKER;
import static no.nav.syfo.api.selvbetjening.mapper.RSBrukerOppfolgingsplanMapper.oppfolgingsplan2rs;
import static no.nav.syfo.model.Varseltype.*;
import static no.nav.syfo.util.MapUtil.mapListe;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class OppfolgingsplanService {

    private static final Logger log = getLogger(OppfolgingsplanService.class);

    private OppfolgingsplanDAO oppfolgingsplanDAO;

    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;

    private GodkjentplanDAO godkjentplanDAO;

    private TiltakDAO tiltakDAO;

    private NarmesteLederConsumer narmesteLederConsumer;

    private TilgangskontrollService tilgangskontrollService;

    private AktorregisterConsumer aktorregisterConsumer;

    private FastlegeService fastlegeService;

    private DialogmeldingService dialogmeldingService;

    private ServiceVarselService serviceVarselService;

    private NarmesteLederVarselService narmesteLederVarselService;

    private final PdlConsumer pdlConsumer;

    private GodkjenningerDAO godkjenningerDAO;

    private KommentarDAO kommentarDAO;

    private DokumentDAO dokumentDAO;

    @Inject
    public OppfolgingsplanService(
            ArbeidsoppgaveDAO arbeidsoppgaveDAO,
            DokumentDAO dokumentDAO,
            KommentarDAO kommentarDAO,
            GodkjenningerDAO godkjenningerDAO,
            GodkjentplanDAO godkjentplanDAO,
            OppfolgingsplanDAO oppfolgingsplanDAO,
            TiltakDAO tiltakDAO,
            AktorregisterConsumer aktorregisterConsumer,
            FastlegeService fastlegeService,
            DialogmeldingService dialogmeldingService,
            NarmesteLederConsumer narmesteLederConsumer,
            PdlConsumer pdlConsumer,
            ServiceVarselService serviceVarselService,
            NarmesteLederVarselService narmesteLederVarselService,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.arbeidsoppgaveDAO = arbeidsoppgaveDAO;
        this.dokumentDAO = dokumentDAO;
        this.kommentarDAO = kommentarDAO;
        this.godkjenningerDAO = godkjenningerDAO;
        this.godkjentplanDAO = godkjentplanDAO;
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.tiltakDAO = tiltakDAO;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.fastlegeService = fastlegeService;
        this.dialogmeldingService = dialogmeldingService;
        this.narmesteLederConsumer = narmesteLederConsumer;
        this.pdlConsumer = pdlConsumer;
        this.serviceVarselService = serviceVarselService;
        this.narmesteLederVarselService = narmesteLederVarselService;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    public List<Oppfolgingsplan> hentAktorsOppfolgingsplaner(BrukerkontekstConstant brukerkontekst, String innloggetFnr) {
        String innloggetAktorId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        if (ARBEIDSGIVER == brukerkontekst) {
            return arbeidsgiversOppfolgingsplaner(innloggetAktorId, innloggetFnr);
        }

        if (ARBEIDSTAKER == brukerkontekst) {
            return arbeidstakersOppfolgingsplaner(innloggetAktorId);
        }

        return emptyList();
    }

    private List<Oppfolgingsplan> arbeidsgiversOppfolgingsplaner(String aktorId, String fnr) {
        List<Oppfolgingsplan> oppfoelgingsdialoger = new ArrayList<>();
        List<Ansatt> ansatte = narmesteLederConsumer.ansatte(fnr);

        ansatte.forEach(ansatt -> {
            String ansattAktorId = aktorregisterConsumer.hentAktorIdForFnr(ansatt.fnr);
            List<Oppfolgingsplan> ansattesOppfolgingsplaner = oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldt(ansattAktorId).stream()
                    .filter(oppfoelgingsdialog -> oppfoelgingsdialog.virksomhet.virksomhetsnummer.equals(ansatt.virksomhetsnummer))
                    .map(oppfoelgingsdialog -> oppfolgingsplanDAO.populate(oppfoelgingsdialog))
                    .peek(oppfoelgingsdialog -> oppfolgingsplanDAO.oppdaterSistAksessert(oppfoelgingsdialog, aktorId))
                    .collect(toList());
            oppfoelgingsdialoger.addAll(ansattesOppfolgingsplaner);
        });
        return oppfoelgingsdialoger;
    }

    private List<Oppfolgingsplan> arbeidstakersOppfolgingsplaner(String aktorId) {
        return oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldt(aktorId).stream()
                .peek(oppfoelgingsdialog -> oppfolgingsplanDAO.oppdaterSistAksessert(oppfoelgingsdialog, aktorId))
                .map(oppfoelgingsdialog -> oppfolgingsplanDAO.populate(oppfoelgingsdialog))
                .collect(toList());
    }

    public Oppfolgingsplan hentOppfoelgingsdialog(Long oppfoelgingsdialogId) {
        return oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);
    }

    public Oppfolgingsplan hentGodkjentOppfolgingsplan(Long oppfoelgingsdialogId) {
        return oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId)
                .godkjentPlan(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfoelgingsdialogId));
    }

    @Transactional
    public Long opprettOppfolgingsplan(RSOpprettOppfoelgingsdialog rsOpprettOppfolgingsplan, String innloggetFnr) {
        String virksomhetsnummer = rsOpprettOppfolgingsplan.virksomhetsnummer;
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);
        String sykmeldtFnr = rsOpprettOppfolgingsplan.sykmeldtFnr;
        String sykmeldtAktoerId = innloggetFnr.equals(sykmeldtFnr)
                ? innloggetAktoerId
                : aktorregisterConsumer.hentAktorIdForFnr(sykmeldtFnr);

        if (pdlConsumer.isKode6Or7(aktorregisterConsumer.hentFnrForAktor(sykmeldtAktoerId))) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (!tilgangskontrollService.kanOppretteOppfolgingsplan(sykmeldtFnr, innloggetFnr, virksomhetsnummer)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (parteneHarEkisterendeAktivOppfolgingsplan(sykmeldtAktoerId, virksomhetsnummer)) {
            log.warn("Kan ikke opprette en plan når det allerede eksisterer en aktiv plan mellom partene!");
            throw new ConflictException();
        }

        return opprettDialog(sykmeldtAktoerId, virksomhetsnummer, innloggetAktoerId);
    }

    @Transactional
    public long kopierOppfoelgingsdialog(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.brukerTilhorerOppfolgingsplan(innloggetFnr, oppfolgingsplan)) {
            throw new ForbiddenException();
        }
        String sykmeldtAktoerId = oppfolgingsplan.arbeidstaker.aktoerId;

        if (parteneHarEkisterendeAktivOppfolgingsplan(sykmeldtAktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer)) {
            log.warn("Kan ikke opprette en plan når det allerede eksisterer en aktiv plan mellom partene!");
            throw new ConflictException();
        }

        long nyOppfoelgingsdialogId = opprettDialog(oppfolgingsplan.arbeidstaker.aktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer, innloggetAktoerId);
        overfoerDataFraDialogTilNyDialog(oppfoelgingsdialogId, nyOppfoelgingsdialogId);

        return nyOppfoelgingsdialogId;
    }

    @Transactional
    public void overfoerDataFraDialogTilNyDialog(long gammelOppfoelgingsdialogId, long nyOppfoelgingsdialogId) {
        arbeidsoppgaveDAO.arbeidsoppgaverByOppfoelgingsdialogId(gammelOppfoelgingsdialogId)
                .forEach(arbeidsoppgave -> arbeidsoppgaveDAO.create(arbeidsoppgave.oppfoelgingsdialogId(nyOppfoelgingsdialogId)));
        tiltakDAO.finnTiltakByOppfoelgingsdialogId(gammelOppfoelgingsdialogId)
                .forEach(tiltak -> {
                    Tiltak nyttTiltak = tiltakDAO.create(tiltak.oppfoelgingsdialogId(nyOppfoelgingsdialogId));
                    tiltak.kommentarer.forEach(kommentar -> kommentarDAO.create(kommentar.tiltakId(nyttTiltak.id)));
                });
    }

    private Long opprettDialog(String sykmeldtAktorId, String virksomhetsnummer, String innloggetAktorId) {
        Oppfolgingsplan oppfolgingsplan = new Oppfolgingsplan()
                .sistEndretAvAktoerId(innloggetAktorId)
                .opprettetAvAktoerId(innloggetAktorId)
                .arbeidstaker(new Person().aktoerId(sykmeldtAktorId))
                .virksomhet(new Virksomhet().virksomhetsnummer(virksomhetsnummer));
        if (innloggetAktorId.equals(sykmeldtAktorId)) {
            oppfolgingsplan.arbeidstaker.sistInnlogget(now());
            oppfolgingsplan.arbeidstaker.sisteEndring(now());
            oppfolgingsplan.arbeidstaker.sistAksessert(now());
        } else {
            oppfolgingsplan.arbeidsgiver.sistInnlogget(now());
            oppfolgingsplan.arbeidsgiver.sisteEndring(now());
            oppfolgingsplan.arbeidsgiver.sistAksessert(now());
        }
        oppfolgingsplan = oppfolgingsplanDAO.create(oppfolgingsplan);
        sendVarsler(innloggetAktorId, oppfolgingsplan);
        return oppfolgingsplan.id;
    }

    private void sendVarsler(String innloggetAktoerId, Oppfolgingsplan oppfolgingsplan) {
        if (innloggetAktoerId.equals(oppfolgingsplan.arbeidstaker.aktoerId)) {
            String arbeidstakersFnr = aktorregisterConsumer.hentFnrForAktor(oppfolgingsplan.arbeidstaker.aktoerId);
            Naermesteleder naermesteleder = narmesteLederConsumer.narmesteLeder(arbeidstakersFnr, oppfolgingsplan.virksomhet.virksomhetsnummer).get();
            narmesteLederVarselService.sendVarselTilNaermesteLeder(SyfoplanOpprettetNL, naermesteleder);
        } else {
            serviceVarselService.sendServiceVarsel(oppfolgingsplan.arbeidstaker.aktoerId, SyfoplanOpprettetSyk, oppfolgingsplan.id);
        }
    }

    private boolean parteneHarEkisterendeAktivOppfolgingsplan(String sykmeldtAktoerId, String virksomhetsnummer) {
        return oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldtogVirksomhet(sykmeldtAktoerId, virksomhetsnummer).stream()
                .map(oppfoelgingsdialog -> godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfoelgingsdialog.id))
                .anyMatch(maybeGodkjentplan -> erIkkeFerdigVersjon(maybeGodkjentplan) || erIkkeAvbruttOgIkkeUtgaatt(maybeGodkjentplan));
    }

    private boolean erIkkeAvbruttOgIkkeUtgaatt(Optional<GodkjentPlan> maybeGodkjentplan) {
        return !maybeGodkjentplan.get().avbruttPlan.isPresent() && maybeGodkjentplan.get().gyldighetstidspunkt.tom.isAfter(LocalDate.now());
    }

    private boolean erIkkeFerdigVersjon(Optional<GodkjentPlan> maybeGodkjentplan) {
        return !maybeGodkjentplan.isPresent();
    }

    @Transactional
    public void delMedNav(long oppfolgingsplanId, String innloggetFnr) {
        Oppfolgingsplan oppfoelgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);
        LocalDateTime deltMedNavTidspunkt = now();

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetFnr, oppfoelgingsplan);

        godkjentplanDAO.delMedNav(oppfolgingsplanId, deltMedNavTidspunkt);
        godkjentplanDAO.delMedNavTildelEnhet(oppfoelgingsplan.id);
        oppfolgingsplanDAO.sistEndretAv(oppfolgingsplanId, innloggetAktoerId);

    }

    @Transactional
    public void delMedFastlege(long oppfolgingsplanId, String innloggetFnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetFnr, oppfolgingsplan);

        String sendesTilAktoerId = oppfolgingsplan.arbeidstaker.aktoerId;
        String sendesTilFnr = aktorregisterConsumer.hentFnrForAktor(sendesTilAktoerId);

        byte[] pdf = godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfolgingsplanId)
                .map(GodkjentPlan::dokumentUuid)
                .map(dokumentDAO::hent)
                .orElseThrow(() -> new RuntimeException("Finner ikke pdf for oppfølgingsplan med id " + oppfolgingsplanId));

        dialogmeldingService.sendOppfolgingsplanTilFastlege(sendesTilFnr, pdf);

        godkjentplanDAO.delMedFastlege(oppfolgingsplanId);
    }

    @Transactional
    public void nullstillGodkjenning(long oppfolgingsplanId, String fnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(fnr);

        throwExceptionWithoutAccessToOppfolgingsplan(fnr, oppfolgingsplan);

        oppfolgingsplanDAO.sistEndretAv(oppfolgingsplanId, innloggetAktoerId);
        oppfolgingsplanDAO.nullstillSamtykke(oppfolgingsplanId);
        godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfolgingsplanId);
    }

    public void oppdaterSistInnlogget(long oppfolgingsplanId, String fnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(fnr);

        throwExceptionWithoutAccessToOppfolgingsplan(fnr, oppfolgingsplan);

        oppfolgingsplanDAO.oppdaterSistInnlogget(oppfolgingsplan, innloggetAktoerId);
    }

    @Transactional
    public void avbrytPlan(long oppfolgingsplanId, String innloggetFnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetFnr, oppfolgingsplan);

        oppfolgingsplanDAO.avbryt(oppfolgingsplan.id, innloggetAktoerId);
        long nyOppfolgingsplanId = opprettDialog(oppfolgingsplan.arbeidstaker.aktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer, innloggetAktoerId);
        overfoerDataFraDialogTilNyDialog(oppfolgingsplanId, nyOppfolgingsplanId);
    }

    public RSGyldighetstidspunkt hentGyldighetstidspunktForGodkjentPlan(Long id, BrukerkontekstConstant arbeidsgiver, String innloggetIdent) {
        RSBrukerOppfolgingsplan oppfolgingsplan = mapListe(hentAktorsOppfolgingsplaner(arbeidsgiver, innloggetIdent), oppfolgingsplan2rs)
                .stream()
                .filter(plan -> plan.id.equals(id))
                .findFirst().orElseThrow(NotFoundException::new);
        return oppfolgingsplan.godkjentPlan != null ? oppfolgingsplan.godkjentPlan.gyldighetstidspunkt : null;
    }

    public boolean harBrukerTilgangTilDialog(long oppfolgingsplanId, String fnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        return tilgangskontrollService.brukerTilhorerOppfolgingsplan(fnr, oppfolgingsplan);
    }

    private void throwExceptionWithoutAccessToOppfolgingsplan(String fnr, Oppfolgingsplan oppfolgingsplan) {
        if (!tilgangskontrollService.brukerTilhorerOppfolgingsplan(fnr, oppfolgingsplan)) {
            throw new ForbiddenException();
        }
    }
}
