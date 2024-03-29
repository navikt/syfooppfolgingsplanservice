package no.nav.syfo.service;

import no.nav.syfo.dialogmelding.DialogmeldingService;
import no.nav.syfo.domain.*;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.util.ConflictException;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class OppfolgingsplanService {

    private static final Logger log = getLogger(OppfolgingsplanService.class);

    private final OppfolgingsplanDAO oppfolgingsplanDAO;

    private final ArbeidsoppgaveDAO arbeidsoppgaveDAO;

    private final GodkjentplanDAO godkjentplanDAO;

    private final TiltakDAO tiltakDAO;

    private final TilgangskontrollService tilgangskontrollService;

    private final DialogmeldingService dialogmeldingService;

    private final PdlConsumer pdlConsumer;

    private final GodkjenningerDAO godkjenningerDAO;

    private final KommentarDAO kommentarDAO;

    private final DokumentDAO dokumentDAO;

    @Inject
    public OppfolgingsplanService(
            ArbeidsoppgaveDAO arbeidsoppgaveDAO,
            DokumentDAO dokumentDAO,
            KommentarDAO kommentarDAO,
            GodkjenningerDAO godkjenningerDAO,
            GodkjentplanDAO godkjentplanDAO,
            OppfolgingsplanDAO oppfolgingsplanDAO,
            TiltakDAO tiltakDAO,
            DialogmeldingService dialogmeldingService,
            PdlConsumer pdlConsumer,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.arbeidsoppgaveDAO = arbeidsoppgaveDAO;
        this.dokumentDAO = dokumentDAO;
        this.kommentarDAO = kommentarDAO;
        this.godkjenningerDAO = godkjenningerDAO;
        this.godkjentplanDAO = godkjentplanDAO;
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.tiltakDAO = tiltakDAO;
        this.dialogmeldingService = dialogmeldingService;
        this.pdlConsumer = pdlConsumer;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    public List<Oppfolgingsplan> arbeidsgiversOppfolgingsplanerPaFnr(String lederFnr, String ansattFnr, String virksomhetsnummer) {
        String lederAktorId = pdlConsumer.aktorid(lederFnr);
        String ansattAktorId = pdlConsumer.aktorid(ansattFnr);

        if (!tilgangskontrollService.erNaermesteLederForSykmeldt(lederFnr, ansattFnr, virksomhetsnummer)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        return oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldtogVirksomhet(ansattAktorId, virksomhetsnummer)
                .stream()
                .map(oppfolgingsplanDAO::populate)
                .peek(oppfolgingsplan -> oppfolgingsplanDAO.oppdaterSistAksessert(oppfolgingsplan, lederAktorId))
                .collect(toList());
    }

    public List<Oppfolgingsplan> arbeidstakersOppfolgingsplaner(String innloggetFnr) {
        String innloggetAktorId = pdlConsumer.aktorid(innloggetFnr);
        return oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldt(innloggetAktorId).stream()
                .peek(oppfolgingsplan -> oppfolgingsplanDAO.oppdaterSistAksessert(oppfolgingsplan, innloggetAktorId))
                .map(oppfolgingsplanDAO::populate)
                .collect(toList());
    }

    public Oppfolgingsplan hentGodkjentOppfolgingsplan(Long oppfoelgingsdialogId) {
        return oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId)
                .godkjentPlan(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfoelgingsdialogId));
    }

    @Transactional
    public Long opprettOppfolgingsplan(String innloggetFnr, String virksomhetsnummer, String sykmeldtFnr) {
        String innloggetAktoerId = pdlConsumer.aktorid(innloggetFnr);
        String sykmeldtAktoerId = innloggetFnr.equals(sykmeldtFnr)
                ? innloggetAktoerId
                : pdlConsumer.aktorid(sykmeldtFnr);

        if (!tilgangskontrollService.kanOppretteOppfolgingsplan(sykmeldtFnr, innloggetFnr, virksomhetsnummer)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (parteneHarEkisterendeAktivOppfolgingsplan(sykmeldtAktoerId, virksomhetsnummer)) {
            log.warn("Kan ikke opprette en plan når det allerede eksisterer en aktiv plan mellom partene!");
            throw new ConflictException();
        }

        return opprettDialog(sykmeldtAktoerId, sykmeldtFnr, virksomhetsnummer, innloggetAktoerId, innloggetFnr);
    }

    @Transactional
    public long kopierOppfoelgingsdialog(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = pdlConsumer.aktorid(innloggetFnr);

        if (!tilgangskontrollService.brukerTilhorerOppfolgingsplan(innloggetFnr, oppfolgingsplan)) {
            throw new ForbiddenException();
        }
        String sykmeldtAktoerId = oppfolgingsplan.arbeidstaker.aktoerId;
        String sykmeldtFnr = oppfolgingsplan.arbeidstaker.fnr != null ? oppfolgingsplan.arbeidstaker.fnr : pdlConsumer.fnr(sykmeldtAktoerId);

        if (parteneHarEkisterendeAktivOppfolgingsplan(sykmeldtAktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer)) {
            log.warn("Kan ikke opprette en plan når det allerede eksisterer en aktiv plan mellom partene!");
            throw new ConflictException();
        }

        long nyOppfoelgingsdialogId = opprettDialog(oppfolgingsplan.arbeidstaker.aktoerId, sykmeldtFnr, oppfolgingsplan.virksomhet.virksomhetsnummer, innloggetAktoerId,
                innloggetFnr);
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

    private Long opprettDialog(String sykmeldtAktorId, String sykmeldtFnr, String virksomhetsnummer, String innloggetAktorId, String innloggetFnr) {
        Oppfolgingsplan oppfolgingsplan = new Oppfolgingsplan()
                .sistEndretAvAktoerId(innloggetAktorId)
                .sistEndretAvFnr(innloggetFnr)
                .opprettetAvAktoerId(innloggetAktorId)
                .opprettetAvFnr(innloggetFnr)
                .arbeidstaker(new Person().aktoerId(sykmeldtAktorId).fnr(sykmeldtFnr))
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
        return oppfolgingsplan.id;
    }

    private boolean parteneHarEkisterendeAktivOppfolgingsplan(String sykmeldtAktoerId, String virksomhetsnummer) {
        return oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldtogVirksomhet(sykmeldtAktoerId, virksomhetsnummer).stream()
                .map(oppfoelgingsdialog -> godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfoelgingsdialog.id))
                .anyMatch(maybeGodkjentplan -> erIkkeFerdigVersjon(maybeGodkjentplan) || erIkkeAvbruttOgIkkeUtgaatt(maybeGodkjentplan));
    }

    private boolean erIkkeAvbruttOgIkkeUtgaatt(Optional<GodkjentPlan> maybeGodkjentplan) {
        return maybeGodkjentplan.isPresent()
                && maybeGodkjentplan.get().avbruttPlan.isEmpty()
                && maybeGodkjentplan.get().gyldighetstidspunkt.tom.isAfter(LocalDate.now());
    }

    private boolean erIkkeFerdigVersjon(Optional<GodkjentPlan> maybeGodkjentplan) {
        return maybeGodkjentplan.isEmpty();
    }

    @Transactional
    public void delMedNav(long oppfolgingsplanId, String innloggetFnr) {
        Oppfolgingsplan oppfoelgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = pdlConsumer.aktorid(innloggetFnr);
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

        String arbeidstakerAktoerId = oppfolgingsplan.arbeidstaker.aktoerId;
        String arbeidstakerFnr = pdlConsumer.fnr(arbeidstakerAktoerId);

        byte[] pdf = godkjentplanDAO.godkjentPlanByOppfolgingsplanId(oppfolgingsplanId)
                .map(GodkjentPlan::dokumentUuid)
                .map(dokumentDAO::hent)
                .orElseThrow(() -> new RuntimeException("Finner ikke pdf for oppfølgingsplan med id " + oppfolgingsplanId));

        dialogmeldingService.sendOppfolgingsplanTilFastlege(arbeidstakerFnr, pdf);

        godkjentplanDAO.delMedFastlege(oppfolgingsplanId);
    }

    @Transactional
    public void nullstillGodkjenning(long oppfolgingsplanId, String fnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = pdlConsumer.aktorid(fnr);

        throwExceptionWithoutAccessToOppfolgingsplan(fnr, oppfolgingsplan);

        oppfolgingsplanDAO.sistEndretAv(oppfolgingsplanId, innloggetAktoerId);
        oppfolgingsplanDAO.nullstillSamtykke(oppfolgingsplanId);
        godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfolgingsplanId);
    }

    public void oppdaterSistInnlogget(long oppfolgingsplanId, String fnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = pdlConsumer.aktorid(fnr);

        throwExceptionWithoutAccessToOppfolgingsplan(fnr, oppfolgingsplan);

        oppfolgingsplanDAO.oppdaterSistInnlogget(oppfolgingsplan, innloggetAktoerId);
    }

    @Transactional
    public long avbrytPlan(long oppfolgingsplanId, String innloggetFnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfolgingsplanId);
        String innloggetAktoerId = pdlConsumer.aktorid(innloggetFnr);

        throwExceptionWithoutAccessToOppfolgingsplan(innloggetFnr, oppfolgingsplan);

        oppfolgingsplanDAO.avbryt(oppfolgingsplan.id, innloggetAktoerId);
        long nyOppfolgingsplanId = opprettDialog(oppfolgingsplan.arbeidstaker.aktoerId, oppfolgingsplan.arbeidstaker.fnr, oppfolgingsplan.virksomhet.virksomhetsnummer,
                innloggetAktoerId, innloggetFnr);
        overfoerDataFraDialogTilNyDialog(oppfolgingsplanId, nyOppfolgingsplanId);
        return nyOppfolgingsplanId;
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
