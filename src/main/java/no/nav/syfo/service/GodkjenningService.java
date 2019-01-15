package no.nav.syfo.service;

import no.nav.syfo.api.intern.domain.RSGyldighetstidspunkt;
import no.nav.syfo.domain.*;
import no.nav.syfo.model.Kontaktinfo;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.pdf.domain.*;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.repository.domain.Dokument;
import no.nav.syfo.util.ConflictException;
import no.nav.syfo.util.JAXB;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static java.util.Optional.ofNullable;
import static no.nav.metrics.MetricsFactory.createEvent;
import static no.nav.syfo.util.time.DateUtil.tilKortDato;
import static no.nav.syfo.util.time.DateUtil.tilMuntligDatoAarFormat;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.*;
import static no.nav.syfo.model.Varseltype.SyfoplangodkjenningNl;
import static no.nav.syfo.model.Varseltype.SyfoplangodkjenningSyk;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;
import static no.nav.syfo.pdf.PdfFabrikk.tilPdf;
import static no.nav.syfo.util.DatoUtil.antallDagerIPeriode;
import static no.nav.syfo.util.DatoUtil.dagerMellom;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.*;

public class GodkjenningService {

    @Inject
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @Inject
    private NaermesteLederService naermesteLederService;
    @Inject
    private TilgangskontrollService tilgangskontrollService;
    @Inject
    private AktoerService aktoerService;
    @Inject
    private DkifService dkifService;
    @Inject
    private GodkjentplanDAO godkjentplanDAO;
    @Inject
    private DokumentDAO dokumentDAO;
    @Inject
    private BrukerprofilService brukerprofilService;
    @Inject
    private OrganisasjonService organisasjonService;
    @Inject
    private ArbeidsforholdService arbeidsforholdService;
    @Inject
    private ServiceVarselService serviceVarselService;
    @Inject
    private TredjepartsvarselService tredjepartsvarselService;
    @Inject
    private SykeforloepService sykeforloepService;
    @Inject
    private GodkjenningerDAO godkjenningerDAO;
    @Inject
    private AsynkOppgaveDAO asynkOppgaveDAO;

    @Transactional
    public void godkjennOppfoelgingsdialog(long oppfoelgingsdialogId, RSGyldighetstidspunkt gyldighetstidspunkt, String innloggetFnr, boolean tvungenGodkjenning) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (annenPartHarGjortEndringerImellomtiden(oppfoelgingsdialog, innloggetAktoerId)) {
            throw new ConflictException();
        }

        if (innloggetBrukerHarAlleredeGodkjentPlan(oppfoelgingsdialog, innloggetAktoerId)) {
            throw new ConflictException();
        }

        oppfoelgingsdialog = oppfoelingsdialogDAO.populate(oppfoelgingsdialog);
        if (erArbeidsgiveren(oppfoelgingsdialog, innloggetAktoerId) && tvungenGodkjenning) {
            genererTvungenPlan(oppfoelgingsdialog, gyldighetstidspunkt);
            godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfoelgingsdialogId);
            sendGodkjentPlanTilAltinn(oppfoelgingsdialogId);
        } else if (erGodkjentAvAnnenPart(oppfoelingsdialogDAO.populate(oppfoelgingsdialog), innloggetAktoerId)) {
            genererNyPlan(oppfoelgingsdialog, innloggetAktoerId);
            godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfoelgingsdialogId);
            sendGodkjentPlanTilAltinn(oppfoelgingsdialogId);
        } else {
            godkjenningerDAO.create(new Godkjenning()
                    .oppfoelgingsdialogId(oppfoelgingsdialogId)
                    .godkjent(true)
                    .godkjentAvAktoerId(innloggetAktoerId)
                    .gyldighetstidspunkt(new Gyldighetstidspunkt()
                            .fom(gyldighetstidspunkt.fom)
                            .tom(gyldighetstidspunkt.tom)
                            .evalueres(gyldighetstidspunkt.evalueres)
                    )
            );
            if (erArbeidsgiveren(oppfoelgingsdialog, innloggetAktoerId)) {
                serviceVarselService.sendServiceVarsel(oppfoelgingsdialog.arbeidstaker.aktoerId, SyfoplangodkjenningSyk, oppfoelgingsdialogId);
            } else {
                Naermesteleder naermesteleder = naermesteLederService.hentNaermesteLeder(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer).get();
                tredjepartsvarselService.sendVarselTilNaermesteLeder(SyfoplangodkjenningNl, naermesteleder, oppfoelgingsdialogId);
            }
        }
        oppfoelingsdialogDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);
    }

    private boolean innloggetBrukerHarAlleredeGodkjentPlan(Oppfoelgingsdialog oppfoelgingsdialog, String innloggetAktoerId) {
        return godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfoelgingsdialog.id).stream().anyMatch(godkjenning -> godkjenning.godkjent && godkjenning.godkjentAvAktoerId.equals(innloggetAktoerId));
    }

    private Godkjenning finnGodkjenning(Oppfoelgingsdialog oppfoelgingsdialog) {
        return oppfoelgingsdialog.godkjenninger.stream()
                .filter(pGodkjenning -> pGodkjenning.godkjent)
                .findFirst().orElseThrow(() -> new RuntimeException("Fant ikke godkjenning"));
    }

    private void rapporterMetrikkerForNyPlan(Oppfoelgingsdialog oppfoelgingsdialog, boolean erPlanTvungenGodkjent) {
        if (erPlanTvungenGodkjent) {
            createEvent("genererTvungenPlan").report();
        } else {
            createEvent("genererNyPlan").report();
        }

        createEvent("tiltak").addFieldToReport("antall", oppfoelgingsdialog.tiltakListe.size()).report();
        createEvent("arbeidsoppgaver").addFieldToReport("antall", oppfoelgingsdialog.arbeidsoppgaveListe.size()).report();

        long antallArboppgGjennomforNormalt = oppfoelgingsdialog.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> KAN.name().equals(arbeidsoppgave.gjennomfoering.gjennomfoeringStatus))
                .count();
        long antallArboppgGjennomforTilrettelegging = oppfoelgingsdialog.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> TILRETTELEGGING.name().equals(arbeidsoppgave.gjennomfoering.gjennomfoeringStatus))
                .count();
        long antallArboppgGjennomforIkke = oppfoelgingsdialog.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> KAN_IKKE.name().equals(arbeidsoppgave.gjennomfoering.gjennomfoeringStatus))
                .count();
        createEvent("arbeidsoppgaverGjennomforesNormalt").addFieldToReport("antall", antallArboppgGjennomforNormalt).report();
        createEvent("arbeidsoppgaverGjennomforesTilrettelegging").addFieldToReport("antall", antallArboppgGjennomforTilrettelegging).report();
        createEvent("arbeidsoppgaverGjennomforesIkke").addFieldToReport("antall", antallArboppgGjennomforIkke).report();

        String arbeidstakerAktoerId = oppfoelgingsdialog.arbeidstaker.aktoerId;

        long antallArbboppgVurdertOgOpprettetAvAT = oppfoelgingsdialog.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> arbeidsoppgave.erVurdertAvSykmeldt && arbeidsoppgave.opprettetAvAktoerId.equals(arbeidstakerAktoerId))
                .count();
        long antallArbboppgVurdertOgOpprettetAvNL = oppfoelgingsdialog.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> arbeidsoppgave.erVurdertAvSykmeldt && !arbeidsoppgave.opprettetAvAktoerId.equals(arbeidstakerAktoerId))
                .count();
        long antallArbboppgIkkeVurdertOgOpprettetAvAT = oppfoelgingsdialog.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> !(arbeidsoppgave.erVurdertAvSykmeldt || arbeidsoppgave.opprettetAvAktoerId.equals(arbeidstakerAktoerId)))
                .count();
        createEvent("arbeidsoppgaverVurdertAvATOpprettetAvAT").addFieldToReport("antall", antallArbboppgVurdertOgOpprettetAvAT).report();
        createEvent("arbeidsoppgaverVurdertAvATOpprettetAvNL").addFieldToReport("antall", antallArbboppgVurdertOgOpprettetAvNL).report();
        createEvent("arbeidsoppgaverIkkeVurdertAvATOpprettetAvNL").addFieldToReport("antall", antallArbboppgIkkeVurdertOgOpprettetAvAT).report();

        List<Kommentar> kommentarListe = oppfoelgingsdialog.tiltakListe
                .stream()
                .flatMap(tiltak -> tiltak.kommentarer.stream())
                .collect(Collectors.toList());
        long antallKommentarerAT = kommentarListe
                .stream()
                .filter(kommentar -> kommentar.opprettetAvAktoerId.equals(arbeidstakerAktoerId))
                .count();
        long antallKommentarerNL = kommentarListe
                .stream()
                .filter(kommentar -> !kommentar.opprettetAvAktoerId.equals(arbeidstakerAktoerId))
                .count();
        createEvent("tiltakKommentarerFraAT").addFieldToReport("antall", antallKommentarerAT).report();
        createEvent("tiltakKommentarerFraNL").addFieldToReport("antall", antallKommentarerNL).report();
    }

    public void genererNyPlan(Oppfoelgingsdialog oppfoelgingsdialog, String innloggetAktoerId) {
        rapporterMetrikkerForNyPlan(oppfoelgingsdialog, false);

        Naermesteleder naermesteleder = naermesteLederService.hentNaermesteLeder(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer)
                .orElseThrow(() -> new RuntimeException("Fant ikke nærmeste leder"));
        Kontaktinfo sykmeldtKontaktinfo = dkifService.hentKontaktinfoAktoerId(oppfoelgingsdialog.arbeidstaker.aktoerId);
        String sykmeldtnavn = brukerprofilService.hentNavnByAktoerId(oppfoelgingsdialog.arbeidstaker.aktoerId);
        String sykmeldtFnr = aktoerService.hentFnrForAktoer(oppfoelgingsdialog.arbeidstaker.aktoerId);
        String virksomhetsnavn = organisasjonService.finnVirksomhetsnavn(oppfoelgingsdialog.virksomhet.virksomhetsnummer);
        String xml = JAXB.marshallDialog(new OppfoelgingsdialogXML()
                .withArbeidsgiverEpost(naermesteleder.epost)
                .withArbeidsgivernavn(naermesteleder.navn)
                .withArbeidsgiverOrgnr(oppfoelgingsdialog.virksomhet.virksomhetsnummer)
                .withVirksomhetsnavn(virksomhetsnavn)
                .withArbeidsgiverTlf(naermesteleder.mobil)
                .withEvalueres(tilMuntligDatoAarFormat(finnGodkjenning(oppfoelgingsdialog).gyldighetstidspunkt.evalueres))
                .withGyldigfra(tilMuntligDatoAarFormat(finnGodkjenning(oppfoelgingsdialog).gyldighetstidspunkt.fom))
                .withGyldigtil(tilMuntligDatoAarFormat(finnGodkjenning(oppfoelgingsdialog).gyldighetstidspunkt.tom))
                .withSykeforloepsperioderXMLList(mapListe(sykeforloepService.hentSykeforlopperiode(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer), periode -> new SykeforloepsperioderXML()
                        .withFom(tilKortDato(periode.fom))
                        .withTom(tilKortDato(periode.tom))
                        .withAntallDager(antallDagerIPeriode(periode.fom, periode.tom))
                        .withGradering(periode.grad)
                        .withBehandlingsdager(periode.behandlingsdager)
                        .withReisetilskudd(periode.reisetilskudd)
                        .withAvventende(periode.avventende != null)
                ))
                .withIkkeTattStillingTilArbeidsoppgaveXML(mapListe(finnIkkeTattStillingTilArbeidsoppgaver(oppfoelgingsdialog.arbeidsoppgaveListe),
                        arbeidsoppgave -> new IkkeTattStillingTilArbeidsoppgaveXML()
                                .withNavn(arbeidsoppgave.navn)))
                .withKanIkkeGjennomfoeresArbeidsoppgaveXMLList(mapListe(finnKanIkkeGjennomfoeresArbeidsoppgaver(oppfoelgingsdialog.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanIkkeGjennomfoeresArbeidsoppgaveXML()
                                .withBeskrivelse(arbeidsoppgave.gjennomfoering.kanIkkeBeskrivelse)
                                .withNavn(arbeidsoppgave.navn)))
                .withKanGjennomfoeresMedTilretteleggingArbeidsoppgaveXMLList(mapListe(finnKanGjennomfoeresMedTilretteleggingArbeidsoppgaver(oppfoelgingsdialog.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML()
                                .withMedHjelp(arbeidsoppgave.gjennomfoering.medHjelp)
                                .withMedMerTid(arbeidsoppgave.gjennomfoering.medMerTid)
                                .withPaaAnnetSted(arbeidsoppgave.gjennomfoering.paaAnnetSted)
                                .withBeskrivelse(arbeidsoppgave.gjennomfoering.kanBeskrivelse)
                                .withNavn(arbeidsoppgave.navn)))
                .withKanGjennomfoeresArbeidsoppgaveXMLList(mapListe(finnKanGjennomfoeresArbeidsoppgaver(oppfoelgingsdialog.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanGjennomfoeresArbeidsoppgaveXML()
                                .withNavn(arbeidsoppgave.navn)))
                .withTiltak(mapListe(oppfoelgingsdialog.tiltakListe, tiltak -> new TiltakXML()
                        .withNavn(tiltak.navn)
                        .withBeskrivelse(tiltak.beskrivelse)
                        .withBeskrivelseIkkeAktuelt(tiltak.beskrivelseIkkeAktuelt)
                        .withStatus(tiltak.status)
                        .withId(tiltak.id)
                        .withGjennomfoering(tiltak.gjennomfoering)
                        .withFom(tilMuntligDatoAarFormat(ofNullable(tiltak.fom).orElse(finnGodkjenning(oppfoelgingsdialog).gyldighetstidspunkt.fom)))
                        .withTom(tilMuntligDatoAarFormat(ofNullable(tiltak.tom).orElse(finnGodkjenning(oppfoelgingsdialog).gyldighetstidspunkt.tom)))
                        .withOpprettetAv(brukerprofilService.hentNavnByAktoerId(tiltak.opprettetAvAktoerId))
                ))
                .withStillingListe(mapListe(arbeidsforholdService.hentArbeidsforholdMedAktoerId(oppfoelgingsdialog.arbeidstaker.aktoerId, finnGodkjenning(oppfoelgingsdialog).gyldighetstidspunkt.fom, oppfoelgingsdialog.virksomhet.virksomhetsnummer), stilling -> new StillingXML()
                        .withYrke(stilling.yrke)
                        .withProsent(stilling.prosent)))
                .withSykmeldtFnr(sykmeldtFnr)
                .withSykmeldtNavn(sykmeldtnavn)
                .withSykmeldtTlf(sykmeldtKontaktinfo.tlf)
                .withSykmeldtEpost(sykmeldtKontaktinfo.epost)
                .withVisAdvarsel(false)
                .withFotnote("Oppfølgningsplanen mellom " + sykmeldtnavn + " og " + naermesteleder.navn)
                .withOpprettetAv(!erArbeidstakeren(oppfoelgingsdialog, innloggetAktoerId) ? sykmeldtnavn : naermesteleder.navn)
                .withOpprettetDato(tilMuntligDatoAarFormat(finnGodkjenning(oppfoelgingsdialog).godkjenningsTidspunkt.toLocalDate()))
                .withGodkjentAv(erArbeidstakeren(oppfoelgingsdialog, innloggetAktoerId) ? sykmeldtnavn : naermesteleder.navn)
                .withGodkjentDato(tilMuntligDatoAarFormat(LocalDate.now()))
        );

        String dokumentUuid = UUID.randomUUID().toString();
        godkjentplanDAO.create(new GodkjentPlan()
                .oppfoelgingsdialogId(oppfoelgingsdialog.id)
                .deltMedNAV(false)
                .deltMedFastlege(false)
                .tvungenGodkjenning(false)
                .dokumentUuid(dokumentUuid)
                .gyldighetstidspunkt(new Gyldighetstidspunkt()
                        .fom(finnGodkjenning(oppfoelgingsdialog).gyldighetstidspunkt.fom)
                        .tom(finnGodkjenning(oppfoelgingsdialog).gyldighetstidspunkt.tom)
                        .evalueres(finnGodkjenning(oppfoelgingsdialog).gyldighetstidspunkt.evalueres)
                ));
        createEvent("opprettettilgodkjent").addFieldToReport("dager", dagerMellom(oppfoelgingsdialog.opprettet, now())).report();
        createEvent("fragodkjenningtilplan").addFieldToReport("dager", dagerMellom(finnGodkjenning(oppfoelgingsdialog).godkjenningsTidspunkt, now())).report();
        dokumentDAO.lagre(new Dokument()
                .uuid(dokumentUuid)
                .pdf(tilPdf(xml))
                .xml(xml)
        );
    }

    public void genererTvungenPlan(Oppfoelgingsdialog oppfoelgingsdialog, RSGyldighetstidspunkt gyldighetstidspunkt) {
        rapporterMetrikkerForNyPlan(oppfoelgingsdialog, true);

        Naermesteleder naermesteleder = naermesteLederService.hentNaermesteLeder(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer)
                .orElseThrow(() -> new RuntimeException("Fant ikke nærmeste leder"));
        Kontaktinfo sykmeldtKontaktinfo = dkifService.hentKontaktinfoAktoerId(oppfoelgingsdialog.arbeidstaker.aktoerId);
        String sykmeldtnavn = brukerprofilService.hentNavnByAktoerId(oppfoelgingsdialog.arbeidstaker.aktoerId);
        String sykmeldtFnr = aktoerService.hentFnrForAktoer(oppfoelgingsdialog.arbeidstaker.aktoerId);
        String virksomhetsnavn = organisasjonService.finnVirksomhetsnavn(oppfoelgingsdialog.virksomhet.virksomhetsnummer);
        String xml = JAXB.marshallDialog(new OppfoelgingsdialogXML()
                .withArbeidsgiverEpost(naermesteleder.epost)
                .withArbeidsgivernavn(naermesteleder.navn)
                .withArbeidsgiverOrgnr(oppfoelgingsdialog.virksomhet.virksomhetsnummer)
                .withVirksomhetsnavn(virksomhetsnavn)
                .withArbeidsgiverTlf(naermesteleder.mobil)
                .withEvalueres(tilMuntligDatoAarFormat(gyldighetstidspunkt.evalueres()))
                .withGyldigfra(tilMuntligDatoAarFormat(gyldighetstidspunkt.fom))
                .withGyldigtil(tilMuntligDatoAarFormat(gyldighetstidspunkt.tom))
                .withSykeforloepsperioderXMLList(mapListe(sykeforloepService.hentSykeforlopperiode(oppfoelgingsdialog.arbeidstaker.aktoerId, oppfoelgingsdialog.virksomhet.virksomhetsnummer), periode -> new SykeforloepsperioderXML()
                        .withFom(tilKortDato(periode.fom))
                        .withTom(tilKortDato(periode.tom))
                        .withAntallDager(antallDagerIPeriode(periode.fom, periode.tom))
                        .withGradering(periode.grad)
                        .withBehandlingsdager(periode.behandlingsdager)
                        .withReisetilskudd(periode.reisetilskudd)
                        .withAvventende(periode.avventende)
                ))
                .withIkkeTattStillingTilArbeidsoppgaveXML(mapListe(finnIkkeTattStillingTilArbeidsoppgaver(oppfoelgingsdialog.arbeidsoppgaveListe),
                        arbeidsoppgave -> new IkkeTattStillingTilArbeidsoppgaveXML()
                                .withNavn(arbeidsoppgave.navn)))
                .withKanIkkeGjennomfoeresArbeidsoppgaveXMLList(mapListe(finnKanIkkeGjennomfoeresArbeidsoppgaver(oppfoelgingsdialog.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanIkkeGjennomfoeresArbeidsoppgaveXML()
                                .withBeskrivelse(arbeidsoppgave.gjennomfoering.kanIkkeBeskrivelse)
                                .withNavn(arbeidsoppgave.navn)))
                .withKanGjennomfoeresMedTilretteleggingArbeidsoppgaveXMLList(mapListe(finnKanGjennomfoeresMedTilretteleggingArbeidsoppgaver(oppfoelgingsdialog.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML()
                                .withMedHjelp(arbeidsoppgave.gjennomfoering.medHjelp)
                                .withMedMerTid(arbeidsoppgave.gjennomfoering.medMerTid)
                                .withPaaAnnetSted(arbeidsoppgave.gjennomfoering.paaAnnetSted)
                                .withBeskrivelse(arbeidsoppgave.gjennomfoering.kanBeskrivelse)
                                .withNavn(arbeidsoppgave.navn)))
                .withKanGjennomfoeresArbeidsoppgaveXMLList(mapListe(finnKanGjennomfoeresArbeidsoppgaver(oppfoelgingsdialog.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanGjennomfoeresArbeidsoppgaveXML()
                                .withNavn(arbeidsoppgave.navn)))
                .withTiltak(mapListe(oppfoelgingsdialog.tiltakListe, tiltak -> new TiltakXML()
                        .withNavn(tiltak.navn)
                        .withBeskrivelse(tiltak.beskrivelse)
                        .withBeskrivelseIkkeAktuelt(tiltak.beskrivelseIkkeAktuelt)
                        .withStatus(tiltak.status)
                        .withId(tiltak.id)
                        .withGjennomfoering(tiltak.gjennomfoering)
                        .withFom(tilMuntligDatoAarFormat(ofNullable(tiltak.fom).orElse(gyldighetstidspunkt.fom)))
                        .withTom(tilMuntligDatoAarFormat(ofNullable(tiltak.tom).orElse(gyldighetstidspunkt.tom)))
                        .withOpprettetAv(brukerprofilService.hentNavnByAktoerId(tiltak.opprettetAvAktoerId))
                ))
                .withStillingListe(mapListe(arbeidsforholdService.hentArbeidsforholdMedAktoerId(oppfoelgingsdialog.arbeidstaker.aktoerId, gyldighetstidspunkt.fom, oppfoelgingsdialog.virksomhet.virksomhetsnummer), stilling -> new StillingXML()
                        .withYrke(stilling.yrke)
                        .withProsent(stilling.prosent)))
                .withSykmeldtFnr(sykmeldtFnr)
                .withFotnote("Oppfølgningsplanen mellom " + sykmeldtnavn + " og " + naermesteleder.navn)
                .withSykmeldtNavn(sykmeldtnavn)
                .withSykmeldtTlf(sykmeldtKontaktinfo.tlf)
                .withSykmeldtEpost(sykmeldtKontaktinfo.epost)
                .withVisAdvarsel(true)
                .withGodkjentAv(naermesteleder.navn)
                .withOpprettetAv(naermesteleder.navn)
                .withOpprettetDato(tilMuntligDatoAarFormat(LocalDate.now()))
                .withGodkjentDato(tilMuntligDatoAarFormat(LocalDate.now()))
        );
        String dokumentUuid = UUID.randomUUID().toString();
        godkjentplanDAO.create(new GodkjentPlan()
                .oppfoelgingsdialogId(oppfoelgingsdialog.id)
                .deltMedNAV(false)
                .deltMedFastlege(false)
                .tvungenGodkjenning(true)
                .dokumentUuid(dokumentUuid)
                .gyldighetstidspunkt(new Gyldighetstidspunkt()
                        .fom(gyldighetstidspunkt.fom)
                        .tom(gyldighetstidspunkt.tom)
                        .evalueres(gyldighetstidspunkt.evalueres)
                ));

        dokumentDAO.lagre(new Dokument()
                .uuid(dokumentUuid)
                .pdf(tilPdf(xml))
                .xml(xml)
        );
    }

    @Transactional
    public void avvisGodkjenning(long oppfoelgingsdialogId, String innloggetFnr, String begrunnelse) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktoerService.hentAktoerIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.aktoerTilhoererDialogen(innloggetAktoerId, oppfoelgingsdialog)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfoelgingsdialogId).size() == 0) {
            throw new ConflictException();
        }
        godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfoelgingsdialogId);
        godkjenningerDAO.create(new Godkjenning()
                .godkjent(false)
                .oppfoelgingsdialogId(oppfoelgingsdialogId)
                .beskrivelse(begrunnelse)
                .godkjentAvAktoerId(innloggetAktoerId)
        );
        oppfoelingsdialogDAO.nullstillSamtykke(oppfoelgingsdialogId);
        oppfoelingsdialogDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);
    }

    private void sendGodkjentPlanTilAltinn(Long oppfoelgingsdialogId) {
        String ressursId = String.valueOf(oppfoelgingsdialogId);
        AsynkOppgave sendOppfoelgingsdialog = asynkOppgaveDAO.create(new AsynkOppgave(OPPFOELGINGSDIALOG_SEND, ressursId));
        asynkOppgaveDAO.create(new AsynkOppgave(OPPFOELGINGSDIALOG_ARKIVER, ressursId, sendOppfoelgingsdialog.id));
    }
}
