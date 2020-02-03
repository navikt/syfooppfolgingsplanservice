package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.api.selvbetjening.domain.*;
import no.nav.syfo.domain.*;
import no.nav.syfo.domain.rs.RSOppfoelgingsplan;
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.util.ConflictException;
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
import static no.nav.syfo.util.OppfoelgingsdialogUtil.erArbeidstakeren;

@Slf4j
@Service
public class OppfolgingsplanService {

    private OppfoelingsdialogDAO oppfoelingsdialogDAO;

    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;

    private GodkjentplanDAO godkjentplanDAO;

    private TiltakDAO tiltakDAO;

    private NarmesteLederConsumer narmesteLederConsumer;

    private TilgangskontrollService tilgangskontrollService;

    private AktorregisterConsumer aktorregisterConsumer;

    private ArbeidsfordelingService arbeidsfordelingService;

    private BrukerprofilService brukerprofilService;

    private FastlegeService fastlegeService;

    private EgenAnsattService egenAnsattService;

    private NorgService norgService;

    private ServiceVarselService serviceVarselService;

    private TredjepartsvarselService tredjepartsvarselService;

    private PersonService personService;

    private GodkjenningerDAO godkjenningerDAO;

    private KommentarDAO kommentarDAO;

    private DokumentDAO dokumentDAO;

    private VeilederBehandlingDAO veilederBehandlingDAO;

    @Inject
    public OppfolgingsplanService(
            ArbeidsoppgaveDAO arbeidsoppgaveDAO,
            DokumentDAO dokumentDAO,
            KommentarDAO kommentarDAO,
            GodkjenningerDAO godkjenningerDAO,
            GodkjentplanDAO godkjentplanDAO,
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            TiltakDAO tiltakDAO,
            VeilederBehandlingDAO veilederBehandlingDAO,
            AktorregisterConsumer aktorregisterConsumer,
            ArbeidsfordelingService arbeidsfordelingService,
            BrukerprofilService brukerprofilService,
            FastlegeService fastlegeService,
            EgenAnsattService egenAnsattService,
            NarmesteLederConsumer narmesteLederConsumer,
            NorgService norgService,
            PersonService personService,
            ServiceVarselService serviceVarselService,
            TredjepartsvarselService tredjepartsvarselService,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.arbeidsoppgaveDAO = arbeidsoppgaveDAO;
        this.dokumentDAO = dokumentDAO;
        this.kommentarDAO = kommentarDAO;
        this.godkjenningerDAO = godkjenningerDAO;
        this.godkjentplanDAO = godkjentplanDAO;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.tiltakDAO = tiltakDAO;
        this.veilederBehandlingDAO = veilederBehandlingDAO;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.arbeidsfordelingService = arbeidsfordelingService;
        this.brukerprofilService = brukerprofilService;
        this.fastlegeService = fastlegeService;
        this.egenAnsattService = egenAnsattService;
        this.narmesteLederConsumer = narmesteLederConsumer;
        this.norgService = norgService;
        this.personService = personService;
        this.serviceVarselService = serviceVarselService;
        this.tredjepartsvarselService = tredjepartsvarselService;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    public List<Oppfoelgingsdialog> hentAktorsOppfolgingsplaner(BrukerkontekstConstant brukerkontekst, String innloggetFnr) {
        String innloggetAktorId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        if (ARBEIDSGIVER == brukerkontekst) {
            return arbeidsgiversOppfolgingsplaner(innloggetAktorId);
        }

        if (ARBEIDSTAKER == brukerkontekst) {
            return arbeidstakersOppfolgingsplaner(innloggetAktorId);
        }

        return emptyList();
    }

    private List<Oppfoelgingsdialog> arbeidsgiversOppfolgingsplaner(String aktorId) {
        List<Oppfoelgingsdialog> oppfoelgingsdialoger = new ArrayList<>();
        List<Ansatt> ansatte = narmesteLederConsumer.ansatte(aktorId);

        ansatte.forEach(ansatt -> oppfoelgingsdialoger.addAll(oppfoelingsdialogDAO.oppfolgingsplanerKnyttetTilSykmeldt(ansatt.aktoerId).stream()
                .filter(oppfoelgingsdialog -> oppfoelgingsdialog.virksomhet.virksomhetsnummer.equals(ansatt.virksomhetsnummer))
                .map(oppfoelgingsdialog -> oppfoelingsdialogDAO.populate(oppfoelgingsdialog))
                .peek(oppfoelgingsdialog -> oppfoelingsdialogDAO.oppdaterSistAksessert(oppfoelgingsdialog, aktorId))
                .collect(toList())));
        return oppfoelgingsdialoger;
    }

    private List<Oppfoelgingsdialog> arbeidstakersOppfolgingsplaner(String aktorId) {
        return oppfoelingsdialogDAO.oppfolgingsplanerKnyttetTilSykmeldt(aktorId).stream()
                .peek(oppfoelgingsdialog -> oppfoelingsdialogDAO.oppdaterSistAksessert(oppfoelgingsdialog, aktorId))
                .map(oppfoelgingsdialog -> oppfoelingsdialogDAO.populate(oppfoelgingsdialog))
                .collect(toList());
    }

    public Oppfoelgingsdialog hentOppfoelgingsdialog(Long oppfoelgingsdialogId) {
        return oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);
    }

    public Oppfoelgingsdialog hentGodkjentOppfolgingsplan(Long oppfoelgingsdialogId) {
        return oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId)
                .godkjentPlan(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfoelgingsdialogId));
    }

    @Transactional
    public Long opprettOppfolgingsplan(RSOpprettOppfoelgingsdialog rsOpprettOppfolgingsplan, String innloggetFnr) {
        String virksomhetsnummer = rsOpprettOppfolgingsplan.virksomhetsnummer;
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);
        String sykmeldtAktoerId = innloggetFnr.equals(rsOpprettOppfolgingsplan.sykmeldtFnr)
                ? innloggetAktoerId
                : aktorregisterConsumer.hentAktorIdForFnr(rsOpprettOppfolgingsplan.sykmeldtFnr);

        if (brukerprofilService.erKode6eller7(aktorregisterConsumer.hentFnrForAktor(sykmeldtAktoerId))) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (!tilgangskontrollService.kanOppretteOppfolgingsplan(sykmeldtAktoerId, innloggetAktoerId, virksomhetsnummer)) {
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
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.aktorTilhorerOppfolgingsplan(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new ForbiddenException();
        }

        long nyOppfoelgingsdialogId = opprettDialog(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer, innloggetAktoerId);
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
        Oppfoelgingsdialog oppfolgingsplan = new Oppfoelgingsdialog()
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
        oppfolgingsplan = oppfoelingsdialogDAO.create(oppfolgingsplan);
        sendVarsler(innloggetAktorId, oppfolgingsplan);
        return oppfolgingsplan.id;
    }

    private void sendVarsler(String innloggetAktoerId, Oppfoelgingsdialog oppfolgingsplan) {
        if (innloggetAktoerId.equals(oppfolgingsplan.arbeidstaker.aktoerId)) {
            Naermesteleder naermesteleder = narmesteLederConsumer.narmesteLeder(oppfolgingsplan.arbeidstaker.aktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer).get();
            tredjepartsvarselService.sendVarselTilNaermesteLeder(SyfoplanOpprettetNL, naermesteleder, oppfolgingsplan.id);
        } else {
            serviceVarselService.sendServiceVarsel(oppfolgingsplan.arbeidstaker.aktoerId, SyfoplanOpprettetSyk, oppfolgingsplan.id);
        }
    }

    private boolean parteneHarEkisterendeAktivOppfolgingsplan(String sykmeldtAktoerId, String virksomhetsnummer) {
        return oppfoelingsdialogDAO.oppfolgingsplanerKnyttetTilSykmeldtogVirksomhet(sykmeldtAktoerId, virksomhetsnummer).stream()
                .map(oppfoelgingsdialog -> godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfoelgingsdialog.id))
                .anyMatch(maybeGodkjentplan -> erIkkeFerdigVersjon(maybeGodkjentplan) || erIkkeAvbruttOgIkkeUtgaatt(maybeGodkjentplan));
    }

    private boolean erIkkeAvbruttOgIkkeUtgaatt(Optional<GodkjentPlan> maybeGodkjentplan) {
        return !maybeGodkjentplan.get().avbruttPlan.isPresent() && maybeGodkjentplan.get().gyldighetstidspunkt.tom.isAfter(LocalDate.now());
    }

    private boolean erIkkeFerdigVersjon(Optional<GodkjentPlan> maybeGodkjentplan) {
        return !maybeGodkjentplan.isPresent();
    }

    private Enhet finnBehandlendeEnhetForGeografiskTilknytting(Oppfoelgingsdialog oppfoelgingsdialog) {
        String sykmeldtFnr = aktorregisterConsumer.hentFnrForAktor(oppfoelgingsdialog.arbeidstaker.aktoerId);
        String geografiskTilknytning = personService.hentGeografiskTilknytning(sykmeldtFnr);
        Enhet tildeltEnhet = arbeidsfordelingService.finnBehandlendeEnhet(geografiskTilknytning);
        if (egenAnsattService.erEgenAnsatt(sykmeldtFnr)) {
            tildeltEnhet = norgService.finnSetteKontor(tildeltEnhet.enhetId).orElse(tildeltEnhet);
        }
        return tildeltEnhet;
    }

    @Transactional
    public void delMedNav(long oppfolgingsplanId, String innloggetFnr) {
        Oppfoelgingsdialog oppfoelgingsplan = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);
        LocalDateTime deltMedNavTidspunkt = now();
        Enhet sykmeldtBehandlendeEnhet = finnBehandlendeEnhetForGeografiskTilknytting(oppfoelgingsplan);

        VeilederBehandling veilederBehandling = new VeilederBehandling()
                .godkjentplanId(godkjentplanDAO.godkjentPlanIdByOppfolgingsplanId(oppfolgingsplanId))
                .tildeltEnhet(sykmeldtBehandlendeEnhet.enhetId)
                .opprettetDato(deltMedNavTidspunkt)
                .sistEndret(deltMedNavTidspunkt);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfoelgingsplan);

        godkjentplanDAO.delMedNav(oppfolgingsplanId, deltMedNavTidspunkt);
        veilederBehandlingDAO.opprett(veilederBehandling);
        godkjentplanDAO.delMedNavTildelEnhet(oppfoelgingsplan.id, sykmeldtBehandlendeEnhet.enhetId);
        oppfoelingsdialogDAO.sistEndretAv(oppfolgingsplanId, innloggetAktoerId);

    }

    @Transactional
    public void delMedFastlege(long oppfolgingsplanId, String innloggetFnr) {
        Oppfoelgingsdialog oppfolgingsplan = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfolgingsplan);

        String sendesTilAktoerId = oppfolgingsplan.arbeidstaker.aktoerId;
        String sendesTilFnr = aktorregisterConsumer.hentFnrForAktor(sendesTilAktoerId);

        byte[] pdf = godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfolgingsplanId)
                .map(GodkjentPlan::dokumentUuid)
                .map(dokumentDAO::hent)
                .orElseThrow(() -> new RuntimeException("Finner ikke pdf for oppfølgingsplan med id " + oppfolgingsplanId));

        fastlegeService.sendOppfolgingsplan(new RSOppfoelgingsplan(sendesTilFnr, pdf));

        godkjentplanDAO.delMedFastlege(oppfolgingsplanId);
    }

    @Transactional
    public void nullstillGodkjenning(long oppfolgingsplanId, String fnr) {
        Oppfoelgingsdialog oppfolgingsplan = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(fnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfolgingsplan);

        oppfoelingsdialogDAO.sistEndretAv(oppfolgingsplanId, innloggetAktoerId);
        oppfoelingsdialogDAO.nullstillSamtykke(oppfolgingsplanId);
        godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfolgingsplanId);
    }

    public void oppdaterSistInnlogget(long oppfolgingsplanId, String fnr) {
        Oppfoelgingsdialog oppfolgingsplan = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(fnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfolgingsplan);

        oppfoelingsdialogDAO.oppdaterSistInnlogget(oppfolgingsplan, innloggetAktoerId);
    }

    @Transactional
    public void avbrytPlan(long oppfolgingsplanId, String innloggetFnr) {
        Oppfoelgingsdialog oppfolgingsplan = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfolgingsplan);

        oppfoelingsdialogDAO.avbryt(oppfolgingsplan.id, innloggetAktoerId);
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
        Oppfoelgingsdialog oppfolgingsplan = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String aktoerId = aktorregisterConsumer.hentAktorIdForFnr(fnr);
        return tilgangskontrollService.aktorTilhorerOppfolgingsplan(aktoerId, oppfolgingsplan);
    }

    public void foresporRevidering(long oppfolgingsplanId, String innloggetFnr) {
        Oppfoelgingsdialog oppfolgingsplan = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfolgingsplan);

        if (erArbeidstakeren(oppfolgingsplan, innloggetAktoerId)) {
            Naermesteleder naermesteleder = narmesteLederConsumer.narmesteLeder(oppfolgingsplan.arbeidstaker.aktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer).get();
            tredjepartsvarselService.sendVarselTilNaermesteLeder(SyfoplanRevideringNL, naermesteleder, oppfolgingsplan.id);
        } else {
            serviceVarselService.sendServiceVarsel(oppfolgingsplan.arbeidstaker.aktoerId, SyfoplanRevideringSyk, oppfolgingsplan.id);
        }
    }

    private void throwExceptionWithoutAccessToOppfolgingsplan(String loggedInAktorId, Oppfoelgingsdialog oppfolgingsplan) {
        if (!tilgangskontrollService.aktorTilhorerOppfolgingsplan(loggedInAktorId, oppfolgingsplan)) {
            throw new ForbiddenException();
        }
    }
}