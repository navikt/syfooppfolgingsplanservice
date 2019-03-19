package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.brukerdialog.security.oidc.SystemUserTokenProvider;
import no.nav.syfo.domain.*;
import no.nav.syfo.domain.rs.RSOppfoelgingsplan;
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.service.exceptions.FeilDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.System.getProperty;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.syfo.model.Varseltype.*;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.erArbeidstakeren;
import static no.nav.syfo.util.PropertyUtil.FASTLEGE_DIALOGMELDING_API_URL;

@Slf4j
@Service
public class OppfoelgingsdialogService {

    private OppfoelingsdialogDAO oppfoelingsdialogDAO;

    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;

    private GodkjentplanDAO godkjentplanDAO;

    private TiltakDAO tiltakDAO;

    private NaermesteLederService naermesteLederService;

    private TilgangskontrollService tilgangskontrollService;

    private AktoerService aktoerService;

    private ArbeidsfordelingService arbeidsfordelingService;

    private BrukerprofilService brukerprofilService;

    private EgenAnsattService egenAnsattService;

    private NorgService norgService;

    private ServiceVarselService serviceVarselService;

    private TredjepartsvarselService tredjepartsvarselService;

    private PersonService personService;

    private GodkjenningerDAO godkjenningerDAO;

    private KommentarDAO kommentarDAO;

    private DokumentDAO dokumentDAO;

    private VeilederBehandlingDAO veilederBehandlingDAO;

    private Client client;

    private SystemUserTokenProvider systemUserTokenProvider;

    @Inject
    public OppfoelgingsdialogService(
            Client client,
            SystemUserTokenProvider systemUserTokenProvider,
            ArbeidsoppgaveDAO arbeidsoppgaveDAO,
            DokumentDAO dokumentDAO,
            KommentarDAO kommentarDAO,
            GodkjenningerDAO godkjenningerDAO,
            GodkjentplanDAO godkjentplanDAO,
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            TiltakDAO tiltakDAO,
            VeilederBehandlingDAO veilederBehandlingDAO,
            AktoerService aktoerService,
            ArbeidsfordelingService arbeidsfordelingService,
            BrukerprofilService brukerprofilService,
            EgenAnsattService egenAnsattService,
            NaermesteLederService naermesteLederService,
            NorgService norgService,
            PersonService personService,
            ServiceVarselService serviceVarselService,
            TredjepartsvarselService tredjepartsvarselService,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.client = client;
        this.systemUserTokenProvider = systemUserTokenProvider;
        this.arbeidsoppgaveDAO = arbeidsoppgaveDAO;
        this.dokumentDAO = dokumentDAO;
        this.kommentarDAO = kommentarDAO;
        this.godkjenningerDAO = godkjenningerDAO;
        this.godkjentplanDAO = godkjentplanDAO;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.tiltakDAO = tiltakDAO;
        this.veilederBehandlingDAO = veilederBehandlingDAO;
        this.aktoerService = aktoerService;
        this.arbeidsfordelingService = arbeidsfordelingService;
        this.brukerprofilService = brukerprofilService;
        this.egenAnsattService = egenAnsattService;
        this.naermesteLederService = naermesteLederService;
        this.norgService = norgService;
        this.personService = personService;
        this.serviceVarselService = serviceVarselService;
        this.tredjepartsvarselService = tredjepartsvarselService;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    public List<Oppfoelgingsdialog> hentAktoersOppfoelgingsdialoger(String aktoerId, String brukerkontekst, String innloggetFnr) {
        if ("ARBEIDSGIVER".equals(brukerkontekst)) {
            List<Oppfoelgingsdialog> oppfoelgingsdialoger = new ArrayList<>();
            List<Ansatt> ansatte = naermesteLederService.hentAnsatte(aktoerId);

            ansatte.forEach(ansatt -> oppfoelgingsdialoger.addAll(oppfoelingsdialogDAO.oppfoelgingsdialogerKnyttetTilSykmeldt(ansatt.aktoerId).stream()
                    .filter(oppfoelgingsdialog -> oppfoelgingsdialog.virksomhet.virksomhetsnummer.equals(ansatt.virksomhetsnummer))
                    .map(oppfoelgingsdialog -> oppfoelingsdialogDAO.populate(oppfoelgingsdialog))
                    .peek(oppfoelgingsdialog -> oppfoelingsdialogDAO.oppdaterSistAksessert(oppfoelgingsdialog, aktoerId))
                    .collect(toList())));
            return oppfoelgingsdialoger;
        }

        List<Naermesteleder> tidligereLedere = naermesteLederService.hentNaermesteLedere(aktoerId);
        List<Naermesteleder> inaktiveLedere = tidligereLedere.stream()
                .filter(naermesteleder -> !naermesteleder.naermesteLederStatus.erAktiv)
                .collect(toList());
        List<Naermesteleder> aktiveLedere = tidligereLedere.stream()
                .filter(naermesteleder -> naermesteleder.naermesteLederStatus.erAktiv)
                .collect(toList());

        if ("SYKMELDT".equals(brukerkontekst)) {
            return oppfoelingsdialogDAO.oppfoelgingsdialogerKnyttetTilSykmeldt(aktoerId)
                    .stream()
                    .peek(oppfoelgingsdialog -> inaktiveLedere.stream()
                            .filter(tidligereleder -> tidligereleder.orgnummer.equals(oppfoelgingsdialog.virksomhet.virksomhetsnummer))
                            .filter(tidligereleder -> oppfoelgingsdialog.godkjenninger.stream()
                                    .anyMatch(pGodkjenning -> pGodkjenning.godkjentAvAktoerId.equals(tidligereleder.naermesteLederAktoerId)))
                            .filter(tidligereleder -> aktiveLedere.stream()
                                    .filter(aktivleder -> aktivleder.orgnummer.equals(tidligereleder.orgnummer))
                                    .noneMatch(aktivleder -> aktivleder.naermesteLederAktoerId.equals(tidligereleder.naermesteLederAktoerId))
                            )
                            .forEach(naermesteleder -> nullstillGodkjenning(oppfoelgingsdialog.id, innloggetFnr)))
                    .peek(oppfoelgingsdialog -> oppfoelingsdialogDAO.oppdaterSistAksessert(oppfoelgingsdialog, aktoerId))
                    .map(oppfoelgingsdialog -> oppfoelingsdialogDAO.populate(oppfoelgingsdialog))
                    .collect(toList());
        }

        return emptyList();
    }

    public Oppfoelgingsdialog hentOppfoelgingsdialog(Long oppfoelgingsdialogId) {
        return oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
    }

    public Oppfoelgingsdialog hentGodkjentOppfoelgingsdialog(Long oppfoelgingsdialogId) {
        return oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId)
                .godkjentPlan(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(oppfoelgingsdialogId));
    }

    @Transactional
    public Long opprettOppfoelgingsdialog(String sykmeldtAktoerId, String virksomhetsnummer, String innloggetFnr) {
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(innloggetFnr);
        if (brukerprofilService.erKode6eller7(aktoerService.hentFnrForAktoer(sykmeldtAktoerId))) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (!tilgangskontrollService.kanOppretteDialog(sykmeldtAktoerId, innloggetAktoerId, virksomhetsnummer)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (parteneHarEkisterendeAktivDialog(sykmeldtAktoerId, virksomhetsnummer)) {
            throw new RuntimeException("Kan ikke opprette en dialog når det allerede eksisterer en mellom partene!");
        }

        return opprettDialog(sykmeldtAktoerId, virksomhetsnummer, innloggetAktoerId);
    }

    @Transactional
    public long kopierOppfoelgingsdialog(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new RuntimeException();
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
            Naermesteleder naermesteleder = naermesteLederService.hentNaermesteLeder(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer).get();
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
        String sykmeldtFnr = aktoerService.hentFnrForAktoer(oppfoelgingsdialog.arbeidstaker.aktoerId);
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
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(innloggetFnr);
        LocalDateTime deltMedNavTidspunkt = now();
        Enhet sykmeldtBehandlendeEnhet = finnBehandlendeEnhetForGeografiskTilknytting(oppfoelgingsdialog);

        VeilederBehandling veilederBehandling = new VeilederBehandling()
                .godkjentplanId(godkjentplanDAO.godkjentPlanIdByOppfoelgingsdialogId(oppfoelgingsdialogId))
                .tildeltEnhet(sykmeldtBehandlendeEnhet.enhetId)
                .opprettetDato(deltMedNavTidspunkt)
                .sistEndret(deltMedNavTidspunkt);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new RuntimeException();
        }


        godkjentplanDAO.delMedNav(oppfoelgingsdialogId, deltMedNavTidspunkt);
        veilederBehandlingDAO.opprett(veilederBehandling);
        godkjentplanDAO.delMedNavTildelEnhet(oppfoelgingsdialog.id, sykmeldtBehandlendeEnhet.enhetId);
        oppfoelingsdialogDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);

    }

    @Transactional
    public void delMedFastlege(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new ForbiddenException();
        }

        String sendesTilAktoerId = oppfoelgingsdialog.arbeidstaker.aktoerId;
        String sendesTilFnr = aktoerService.hentFnrForAktoer(sendesTilAktoerId);

        byte[] pdf = godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(oppfoelgingsdialogId)
                .map(GodkjentPlan::dokumentUuid)
                .map(dokumentDAO::hent)
                .orElseThrow(() -> new RuntimeException("Finner ikke pdf for oppfølgingsplan med id " + oppfoelgingsdialogId));

        Response response = client.target(getProperty(FASTLEGE_DIALOGMELDING_API_URL) + "/sendOppfolgingsplan")
                .request()
                .header(AUTHORIZATION, "Bearer " + systemUserTokenProvider.getToken())
                .post(Entity.entity(new RSOppfoelgingsplan(sendesTilFnr, pdf), MediaType.APPLICATION_JSON_TYPE));

        int responsekode = response.getStatus();
        if (responsekode == 500) {
            FeilDTO feilDTO = response.readEntity(FeilDTO.class);
            if (feilDTO.detaljer.detaljertType.contains("FastlegeIkkeFunnet")) {
//                throw new DelOppfoelgingsdialogMedFastlegeFastlegeIkkeFunnet("FastlegeIkkeFunnet", forretningsmessigUnntak(WSFastlegeIkkeFunnet.class, "FastlegeIkkeFunnet"));
                throw new NotFoundException();
            } else if (feilDTO.detaljer.detaljertType.contains("PartnerinformasjonIkkeFunnet")) {
//                throw new DelOppfoelgingsdialogMedFastlegePartnerinformasjonIkkeFunnet("PartnerinformasjonIkkeFunnet", forretningsmessigUnntak(WSPartnerinformasjonIkkeFunnet.class, "PartnerinformasjonIkkeFunnet"));
                throw new NotFoundException();
            }
        }
        if (responsekode >= 300) {
            log.error("Feil ved sending av oppfølgingsdialog til fastlege: Fikk responskode " + responsekode);
            throw new RuntimeException("Feil ved sending av oppfølgingsdialog til fastlege: Fikk responskode " + responsekode);
        }

        godkjentplanDAO.delMedFastlege(oppfoelgingsdialogId);
    }

    @Transactional
    public void nullstillGodkjenning(long oppfoelgingsdialogId, String fnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(fnr);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new RuntimeException();
        }

        oppfoelingsdialogDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);
        oppfoelingsdialogDAO.nullstillSamtykke(oppfoelgingsdialogId);
        godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfoelgingsdialogId);
    }

    public void oppdaterSistInnlogget(long oppfoelgingsdialogId, String fnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(fnr);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new RuntimeException();
        }

        oppfoelingsdialogDAO.oppdaterSistInnlogget(oppfoelgingsdialog, innloggetAktoerId);
    }

    @Transactional
    public void avbrytPlan(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new RuntimeException();
        }

        oppfoelingsdialogDAO.avbryt(oppfoelgingsdialog.id, innloggetAktoerId);
        long nyOppfoelgingsdialogId = opprettDialog(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer, innloggetAktoerId);
        overfoerDataFraDialogTilNyDialog(oppfoelgingsdialogId, nyOppfoelgingsdialogId);
    }

    public boolean harBrukerTilgangTilDialog(long oppfoelgingsdialogId, String fnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String aktoerId = aktoerService.hentAktoerIdForFnr(fnr);
        return tilgangskontrollService.aktoerTilhoererDialogen(aktoerId, oppfoelgingsdialog);
    }

    public void forespoerRevidering(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new RuntimeException();
        }

        if (erArbeidstakeren(oppfoelgingsdialog, innloggetAktoerId)) {
            Naermesteleder naermesteleder = naermesteLederService.hentNaermesteLeder(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer).get();
            tredjepartsvarselService.sendVarselTilNaermesteLeder(SyfoplanRevideringNL, naermesteleder, oppfoelgingsdialog.id);
        } else {
            serviceVarselService.sendServiceVarsel(oppfoelgingsdialog.arbeidstaker.aktoerId, SyfoplanRevideringSyk, oppfoelgingsdialog.id);
        }
    }

}
