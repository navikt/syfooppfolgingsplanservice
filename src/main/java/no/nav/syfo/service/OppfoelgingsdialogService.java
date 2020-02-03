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
public class OppfoelgingsdialogService {

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
    public OppfoelgingsdialogService(
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

    public List<Oppfoelgingsdialog> hentAktoersOppfoelgingsdialoger(BrukerkontekstConstant brukerkontekst, String innloggetFnr) {
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

        ansatte.forEach(ansatt -> oppfoelgingsdialoger.addAll(oppfoelingsdialogDAO.oppfoelgingsdialogerKnyttetTilSykmeldt(ansatt.aktoerId).stream()
                .filter(oppfoelgingsdialog -> oppfoelgingsdialog.virksomhet.virksomhetsnummer.equals(ansatt.virksomhetsnummer))
                .map(oppfoelgingsdialog -> oppfoelingsdialogDAO.populate(oppfoelgingsdialog))
                .peek(oppfoelgingsdialog -> oppfoelingsdialogDAO.oppdaterSistAksessert(oppfoelgingsdialog, aktorId))
                .collect(toList())));
        return oppfoelgingsdialoger;
    }

    private List<Oppfoelgingsdialog> arbeidstakersOppfolgingsplaner(String aktorId) {
        return oppfoelingsdialogDAO.oppfoelgingsdialogerKnyttetTilSykmeldt(aktorId).stream()
                .peek(oppfoelgingsdialog -> oppfoelingsdialogDAO.oppdaterSistAksessert(oppfoelgingsdialog, aktorId))
                .map(oppfoelgingsdialog -> oppfoelingsdialogDAO.populate(oppfoelgingsdialog))
                .collect(toList());
    }

    public Oppfoelgingsdialog hentOppfoelgingsdialog(Long oppfoelgingsdialogId) {
        return oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
    }

    public Oppfoelgingsdialog hentGodkjentOppfoelgingsdialog(Long oppfoelgingsdialogId) {
        return oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId)
                .godkjentPlan(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(oppfoelgingsdialogId));
    }

    @Transactional
    public Long opprettOppfoelgingsdialog(RSOpprettOppfoelgingsdialog rsOpprettOppfoelgingsdialog, String innloggetFnr) {
        String virksomhetsnummer = rsOpprettOppfoelgingsdialog.virksomhetsnummer;
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);
        String sykmeldtAktoerId = innloggetFnr.equals(rsOpprettOppfoelgingsdialog.sykmeldtFnr)
                ? innloggetAktoerId
                : aktorregisterConsumer.hentAktorIdForFnr(rsOpprettOppfoelgingsdialog.sykmeldtFnr);

        if (brukerprofilService.erKode6eller7(aktorregisterConsumer.hentFnrForAktor(sykmeldtAktoerId))) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (!tilgangskontrollService.kanOppretteDialog(sykmeldtAktoerId, innloggetAktoerId, virksomhetsnummer)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (parteneHarEkisterendeAktivDialog(sykmeldtAktoerId, virksomhetsnummer)) {
            log.warn("Kan ikke opprette en plan når det allerede eksisterer en aktiv plan mellom partene!");
            throw new ConflictException();
        }

        return opprettDialog(sykmeldtAktoerId, virksomhetsnummer, innloggetAktoerId);
    }

    @Transactional
    public long kopierOppfoelgingsdialog(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
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

    private Long opprettDialog(String sykmeldtAktoerId, String virksomhetsnummer, String innloggetAktoerId) {
        Oppfoelgingsdialog oppfoelgingsdialog = new Oppfoelgingsdialog()
                .sistEndretAvAktoerId(innloggetAktoerId)
                .opprettetAvAktoerId(innloggetAktoerId)
                .arbeidstaker(new Person().aktoerId(sykmeldtAktoerId))
                .virksomhet(new Virksomhet().virksomhetsnummer(virksomhetsnummer));
        if (innloggetAktoerId.equals(sykmeldtAktoerId)) {
            oppfoelgingsdialog.arbeidstaker.sistInnlogget(now());
            oppfoelgingsdialog.arbeidstaker.sisteEndring(now());
            oppfoelgingsdialog.arbeidstaker.sistAksessert(now());
        } else {
            oppfoelgingsdialog.arbeidsgiver.sistInnlogget(now());
            oppfoelgingsdialog.arbeidsgiver.sisteEndring(now());
            oppfoelgingsdialog.arbeidsgiver.sistAksessert(now());
        }
        oppfoelgingsdialog = oppfoelingsdialogDAO.create(oppfoelgingsdialog);
        sendVarsler(innloggetAktoerId, oppfoelgingsdialog);
        return oppfoelgingsdialog.id;
    }

    private void sendVarsler(String innloggetAktoerId, Oppfoelgingsdialog oppfoelgingsdialog) {
        if (innloggetAktoerId.equals(oppfoelgingsdialog.arbeidstaker.aktoerId)) {
            Naermesteleder naermesteleder = narmesteLederConsumer.narmesteLeder(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer).get();
            tredjepartsvarselService.sendVarselTilNaermesteLeder(SyfoplanOpprettetNL, naermesteleder, oppfoelgingsdialog.id);
        } else {
            serviceVarselService.sendServiceVarsel(oppfoelgingsdialog.arbeidstaker.aktoerId, SyfoplanOpprettetSyk, oppfoelgingsdialog.id);
        }
    }

    private boolean parteneHarEkisterendeAktivDialog(String sykmeldtAktoerId, String virksomhetsnummer) {
        return oppfoelingsdialogDAO.oppfoelgingsdialogerKnyttetTilSykmeldtogVirksomhet(sykmeldtAktoerId, virksomhetsnummer).stream()
                .map(oppfoelgingsdialog -> godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(oppfoelgingsdialog.id))
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
    public void delMedNav(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);
        LocalDateTime deltMedNavTidspunkt = now();
        Enhet sykmeldtBehandlendeEnhet = finnBehandlendeEnhetForGeografiskTilknytting(oppfoelgingsdialog);

        VeilederBehandling veilederBehandling = new VeilederBehandling()
                .godkjentplanId(godkjentplanDAO.godkjentPlanIdByOppfoelgingsdialogId(oppfoelgingsdialogId))
                .tildeltEnhet(sykmeldtBehandlendeEnhet.enhetId)
                .opprettetDato(deltMedNavTidspunkt)
                .sistEndret(deltMedNavTidspunkt);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfoelgingsdialog);

        godkjentplanDAO.delMedNav(oppfoelgingsdialogId, deltMedNavTidspunkt);
        veilederBehandlingDAO.opprett(veilederBehandling);
        godkjentplanDAO.delMedNavTildelEnhet(oppfoelgingsdialog.id, sykmeldtBehandlendeEnhet.enhetId);
        oppfoelingsdialogDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);

    }

    @Transactional
    public void delMedFastlege(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfoelgingsdialog);

        String sendesTilAktoerId = oppfoelgingsdialog.arbeidstaker.aktoerId;
        String sendesTilFnr = aktorregisterConsumer.hentFnrForAktor(sendesTilAktoerId);

        byte[] pdf = godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(oppfoelgingsdialogId)
                .map(GodkjentPlan::dokumentUuid)
                .map(dokumentDAO::hent)
                .orElseThrow(() -> new RuntimeException("Finner ikke pdf for oppfølgingsplan med id " + oppfoelgingsdialogId));

        fastlegeService.sendOppfolgingsplan(new RSOppfoelgingsplan(sendesTilFnr, pdf));

        godkjentplanDAO.delMedFastlege(oppfoelgingsdialogId);
    }

    @Transactional
    public void nullstillGodkjenning(long oppfoelgingsdialogId, String fnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(fnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfoelgingsdialog);

        oppfoelingsdialogDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);
        oppfoelingsdialogDAO.nullstillSamtykke(oppfoelgingsdialogId);
        godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfoelgingsdialogId);
    }

    public void oppdaterSistInnlogget(long oppfoelgingsdialogId, String fnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(fnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfoelgingsdialog);

        oppfoelingsdialogDAO.oppdaterSistInnlogget(oppfoelgingsdialog, innloggetAktoerId);
    }

    @Transactional
    public void avbrytPlan(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfoelgingsdialog);

        oppfoelingsdialogDAO.avbryt(oppfoelgingsdialog.id, innloggetAktoerId);
        long nyOppfoelgingsdialogId = opprettDialog(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer, innloggetAktoerId);
        overfoerDataFraDialogTilNyDialog(oppfoelgingsdialogId, nyOppfoelgingsdialogId);
    }

    public RSGyldighetstidspunkt hentGyldighetstidspunktForGodkjentPlan(Long id, BrukerkontekstConstant arbeidsgiver, String innloggetIdent) {
        RSBrukerOppfolgingsplan oppfoelgingsdialog = mapListe(hentAktoersOppfoelgingsdialoger(arbeidsgiver, innloggetIdent), oppfolgingsplan2rs)
                .stream()
                .filter(plan -> plan.id.equals(id))
                .findFirst().orElseThrow(NotFoundException::new);
        return oppfoelgingsdialog.godkjentPlan != null ? oppfoelgingsdialog.godkjentPlan.gyldighetstidspunkt : null;
    }

    public boolean harBrukerTilgangTilDialog(long oppfoelgingsdialogId, String fnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String aktoerId = aktorregisterConsumer.hentAktorIdForFnr(fnr);
        return tilgangskontrollService.aktoerTilhoererDialogen(aktoerId, oppfoelgingsdialog);
    }

    public void foresporRevidering(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetAktoerId, oppfoelgingsdialog);

        if (erArbeidstakeren(oppfoelgingsdialog, innloggetAktoerId)) {
            Naermesteleder naermesteleder = narmesteLederConsumer.narmesteLeder(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer).get();
            tredjepartsvarselService.sendVarselTilNaermesteLeder(SyfoplanRevideringNL, naermesteleder, oppfoelgingsdialog.id);
        } else {
            serviceVarselService.sendServiceVarsel(oppfoelgingsdialog.arbeidstaker.aktoerId, SyfoplanRevideringSyk, oppfoelgingsdialog.id);
        }
    }

    private void throwExceptionWithoutAccessToOppfolgingsplan(String loggedInAktorId, Oppfoelgingsdialog oppfolgingsplan) {
        if (!tilgangskontrollService.aktoerTilhoererDialogen(loggedInAktorId, oppfolgingsplan)) {
            throw new ForbiddenException();
        }
    }
}
