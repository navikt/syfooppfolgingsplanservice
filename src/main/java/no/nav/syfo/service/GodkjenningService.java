package no.nav.syfo.service;

import no.nav.syfo.aareg.AaregConsumer;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.api.selvbetjening.domain.RSGyldighetstidspunkt;
import no.nav.syfo.dkif.DigitalKontaktinfo;
import no.nav.syfo.dkif.DkifConsumer;
import no.nav.syfo.domain.*;
import no.nav.syfo.ereg.EregConsumer;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.model.Naermesteleder;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.pdf.domain.*;
import no.nav.syfo.repository.dao.*;
import no.nav.syfo.repository.domain.Dokument;
import no.nav.syfo.util.ConflictException;
import no.nav.syfo.util.JAXB;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.*;
import static no.nav.syfo.model.Varseltype.SyfoplangodkjenningNl;
import static no.nav.syfo.model.Varseltype.SyfoplangodkjenningSyk;
import static no.nav.syfo.oidc.OIDCIssuer.EKSTERN;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_ARKIVER;
import static no.nav.syfo.oppgave.Oppgavetype.OPPFOELGINGSDIALOG_SEND;
import static no.nav.syfo.pdf.PdfFabrikk.tilPdf;
import static no.nav.syfo.util.DatoUtil.antallDagerIPeriode;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.*;
import static no.nav.syfo.util.time.DateUtil.tilKortDato;
import static no.nav.syfo.util.time.DateUtil.tilMuntligDatoAarFormat;

@Service
public class GodkjenningService {

    private AaregConsumer aaregConsumer;

    private Metrikk metrikk;

    private OppfolgingsplanDAO oppfolgingsplanDAO;

    private NarmesteLederConsumer narmesteLederConsumer;

    private TilgangskontrollService tilgangskontrollService;

    private AktorregisterConsumer aktorregisterConsumer;

    private DkifConsumer dkifConsumer;

    private GodkjentplanDAO godkjentplanDAO;

    private DokumentDAO dokumentDAO;

    private BrukerprofilService brukerprofilService;

    private EregConsumer eregConsumer;

    private ServiceVarselService serviceVarselService;

    private TredjepartsvarselService tredjepartsvarselService;

    private SykeforloepService sykeforloepService;

    private GodkjenningerDAO godkjenningerDAO;

    private AsynkOppgaveDAO asynkOppgaveDAO;

    @Inject
    public GodkjenningService(
            AaregConsumer aaregConsumer,
            Metrikk metrikk,
            AsynkOppgaveDAO asynkOppgaveDAO,
            DokumentDAO dokumentDAO,
            GodkjenningerDAO godkjenningerDAO,
            GodkjentplanDAO godkjentplanDAO,
            OppfolgingsplanDAO oppfolgingsplanDAO,
            AktorregisterConsumer aktorregisterConsumer,
            BrukerprofilService brukerprofilService,
            DkifConsumer dkifConsumer,
            EregConsumer eregConsumer,
            NarmesteLederConsumer narmesteLederConsumer,
            ServiceVarselService serviceVarselService,
            SykeforloepService sykeforloepService,
            TredjepartsvarselService tredjepartsvarselService,
            TilgangskontrollService tilgangskontrollService
    ) {
        this.aaregConsumer = aaregConsumer;
        this.metrikk = metrikk;
        this.asynkOppgaveDAO = asynkOppgaveDAO;
        this.dokumentDAO = dokumentDAO;
        this.godkjenningerDAO = godkjenningerDAO;
        this.godkjentplanDAO = godkjentplanDAO;
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.brukerprofilService = brukerprofilService;
        this.dkifConsumer = dkifConsumer;
        this.eregConsumer = eregConsumer;
        this.narmesteLederConsumer = narmesteLederConsumer;
        this.serviceVarselService = serviceVarselService;
        this.sykeforloepService = sykeforloepService;
        this.tredjepartsvarselService = tredjepartsvarselService;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @Transactional
    public void godkjennOppfolgingsplan(long oppfoelgingsdialogId, RSGyldighetstidspunkt gyldighetstidspunkt, String innloggetFnr, boolean tvungenGodkjenning, boolean delMedNav) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.aktorTilhorerOppfolgingsplan(innloggetAktoerId, oppfolgingsplan)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (annenPartHarGjortEndringerImellomtiden(oppfolgingsplan, innloggetAktoerId)) {
            throw new ConflictException();
        }

        if (innloggetBrukerHarAlleredeGodkjentPlan(oppfolgingsplan, innloggetAktoerId)) {
            throw new ConflictException();
        }

        oppfolgingsplan = oppfolgingsplanDAO.populate(oppfolgingsplan);

        if (erArbeidsgiveren(oppfolgingsplan, innloggetAktoerId) && tvungenGodkjenning) {
            genererTvungenPlan(oppfolgingsplan, gyldighetstidspunkt, delMedNav);
            godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfoelgingsdialogId);
            sendGodkjentPlanTilAltinn(oppfoelgingsdialogId);

        } else if (erGodkjentAvAnnenPart(oppfolgingsplanDAO.populate(oppfolgingsplan), innloggetAktoerId)) {
            genererNyPlan(oppfolgingsplan, innloggetAktoerId, delMedNav);
            godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfoelgingsdialogId);
            sendGodkjentPlanTilAltinn(oppfoelgingsdialogId);
        } else {
            if (godkjenningRemoved(gyldighetstidspunkt, oppfolgingsplan) || godkjent(oppfolgingsplan)) {
                throw new ConflictException();
            }
            godkjenningerDAO.create(new Godkjenning()
                    .oppfoelgingsdialogId(oppfoelgingsdialogId)
                    .godkjent(true)
                    .delMedNav(delMedNav)
                    .godkjentAvAktoerId(innloggetAktoerId)
                    .gyldighetstidspunkt(new Gyldighetstidspunkt()
                            .fom(gyldighetstidspunkt.fom)
                            .tom(gyldighetstidspunkt.tom)
                            .evalueres(gyldighetstidspunkt.evalueres)
                    )
            );

            if (erArbeidsgiveren(oppfolgingsplan, innloggetAktoerId)) {
                serviceVarselService.sendServiceVarsel(oppfolgingsplan.arbeidstaker.aktoerId, SyfoplangodkjenningSyk, oppfoelgingsdialogId);
            } else {
                Naermesteleder naermesteleder = narmesteLederConsumer.narmesteLeder(oppfolgingsplan.arbeidstaker.aktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer).get();
                tredjepartsvarselService.sendVarselTilNaermesteLeder(SyfoplangodkjenningNl, naermesteleder, oppfoelgingsdialogId);
            }
        }
        oppfolgingsplanDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);
    }

    private boolean godkjent(Oppfolgingsplan oppfolgingsplan) {
        return oppfolgingsplan.godkjentPlan.isPresent();
    }

    private boolean godkjenningRemoved(RSGyldighetstidspunkt gyldighetstidspunkt, Oppfolgingsplan oppfolgingsplan) {
        return gyldighetstidspunkt == null && oppfolgingsplan.godkjenninger.isEmpty();
    }

    private boolean innloggetBrukerHarAlleredeGodkjentPlan(Oppfolgingsplan oppfolgingsplan, String innloggetAktoerId) {
        return godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfolgingsplan.id).stream().anyMatch(godkjenning -> godkjenning.godkjent && godkjenning.godkjentAvAktoerId.equals(innloggetAktoerId));
    }

    private Godkjenning finnGodkjenning(Oppfolgingsplan oppfolgingsplan) {
        return oppfolgingsplan.godkjenninger.stream()
                .filter(pGodkjenning -> pGodkjenning.godkjent)
                .findFirst().orElseThrow(() -> new RuntimeException("Fant ikke godkjenning"));
    }

    private void rapporterMetrikkerForNyPlan(Oppfolgingsplan oppfolgingsplan, boolean erPlanTvungenGodkjent, boolean delMedNav) {
        if (erPlanTvungenGodkjent) {
            metrikk.tellHendelse("genererTvungenPlan");
        } else {
            metrikk.tellHendelse("genererNyPlan");
        }

        if (delMedNav) {
            metrikk.tellHendelse("del_plan_med_nav_ved_generer_godkjent_plan");
        }

        metrikk.tellHendelseMedAntall("tiltak", oppfolgingsplan.tiltakListe.size());
        metrikk.tellHendelseMedAntall("arbeidsoppgaver", oppfolgingsplan.arbeidsoppgaveListe.size());

        long antallArboppgGjennomforNormalt = oppfolgingsplan.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> KAN.name().equals(arbeidsoppgave.gjennomfoering.gjennomfoeringStatus))
                .count();
        long antallArboppgGjennomforTilrettelegging = oppfolgingsplan.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> TILRETTELEGGING.name().equals(arbeidsoppgave.gjennomfoering.gjennomfoeringStatus))
                .count();
        long antallArboppgGjennomforIkke = oppfolgingsplan.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> KAN_IKKE.name().equals(arbeidsoppgave.gjennomfoering.gjennomfoeringStatus))
                .count();

        metrikk.tellHendelseMedAntall("arbeidsoppgaverGjennomforesNormalt", antallArboppgGjennomforNormalt);
        metrikk.tellHendelseMedAntall("arbeidsoppgaverGjennomforesTilrettelegging", antallArboppgGjennomforTilrettelegging);
        metrikk.tellHendelseMedAntall("arbeidsoppgaverGjennomforesIkke", antallArboppgGjennomforIkke);

        String arbeidstakerAktoerId = oppfolgingsplan.arbeidstaker.aktoerId;

        long antallArbboppgVurdertOgOpprettetAvAT = oppfolgingsplan.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> arbeidsoppgave.erVurdertAvSykmeldt && arbeidsoppgave.opprettetAvAktoerId.equals(arbeidstakerAktoerId))
                .count();
        long antallArbboppgVurdertOgOpprettetAvNL = oppfolgingsplan.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> arbeidsoppgave.erVurdertAvSykmeldt && !arbeidsoppgave.opprettetAvAktoerId.equals(arbeidstakerAktoerId))
                .count();
        long antallArbboppgIkkeVurdertOgOpprettetAvAT = oppfolgingsplan.arbeidsoppgaveListe
                .stream()
                .filter(arbeidsoppgave -> !(arbeidsoppgave.erVurdertAvSykmeldt || arbeidsoppgave.opprettetAvAktoerId.equals(arbeidstakerAktoerId)))
                .count();
        metrikk.tellHendelseMedAntall("arbeidsoppgaverVurdertAvATOpprettetAvAT", antallArbboppgVurdertOgOpprettetAvAT);
        metrikk.tellHendelseMedAntall("arbeidsoppgaverVurdertAvATOpprettetAvNL", antallArbboppgVurdertOgOpprettetAvNL);
        metrikk.tellHendelseMedAntall("arbeidsoppgaverIkkeVurdertAvATOpprettetAvNL", antallArbboppgIkkeVurdertOgOpprettetAvAT);

        List<Kommentar> kommentarListe = oppfolgingsplan.tiltakListe
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
        metrikk.tellHendelseMedAntall("tiltakKommentarerFraAT", antallKommentarerAT);
        metrikk.tellHendelseMedAntall("tiltakKommentarerFraNL", antallKommentarerNL);
    }

    public void genererNyPlan(Oppfolgingsplan oppfolgingsplan, String innloggetAktoerId, boolean delMedNav) {
        rapporterMetrikkerForNyPlan(oppfolgingsplan, false, delMedNav);

        Naermesteleder naermesteleder = narmesteLederConsumer.narmesteLeder(oppfolgingsplan.arbeidstaker.aktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer)
                .orElseThrow(() -> new RuntimeException("Fant ikke nærmeste leder"));
        DigitalKontaktinfo sykmeldtKontaktinfo = dkifConsumer.kontaktinformasjon(oppfolgingsplan.arbeidstaker.aktoerId);
        String sykmeldtnavn = brukerprofilService.hentNavnByAktoerId(oppfolgingsplan.arbeidstaker.aktoerId);
        String sykmeldtFnr = aktorregisterConsumer.hentFnrForAktor(oppfolgingsplan.arbeidstaker.aktoerId);
        String virksomhetsnavn = eregConsumer.virksomhetsnavn(oppfolgingsplan.virksomhet.virksomhetsnummer);
        String xml = JAXB.marshallDialog(new OppfoelgingsdialogXML()
                .withArbeidsgiverEpost(naermesteleder.epost)
                .withArbeidsgivernavn(naermesteleder.navn)
                .withArbeidsgiverOrgnr(oppfolgingsplan.virksomhet.virksomhetsnummer)
                .withVirksomhetsnavn(virksomhetsnavn)
                .withArbeidsgiverTlf(naermesteleder.mobil)
                .withEvalueres(tilMuntligDatoAarFormat(finnGodkjenning(oppfolgingsplan).gyldighetstidspunkt.evalueres))
                .withGyldigfra(tilMuntligDatoAarFormat(finnGodkjenning(oppfolgingsplan).gyldighetstidspunkt.fom))
                .withGyldigtil(tilMuntligDatoAarFormat(finnGodkjenning(oppfolgingsplan).gyldighetstidspunkt.tom))
                .withSykeforloepsperioderXMLList(mapListe(sykeforloepService.hentSykeforlopperiode(oppfolgingsplan.arbeidstaker.aktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer, EKSTERN), periode -> new SykeforloepsperioderXML()
                        .withFom(tilKortDato(periode.fom))
                        .withTom(tilKortDato(periode.tom))
                        .withAntallDager(antallDagerIPeriode(periode.fom, periode.tom))
                        .withGradering(periode.grad)
                        .withBehandlingsdager(periode.behandlingsdager)
                        .withReisetilskudd(periode.reisetilskudd)
                        .withAvventende(periode.avventende)
                ))
                .withIkkeTattStillingTilArbeidsoppgaveXML(mapListe(finnIkkeTattStillingTilArbeidsoppgaver(oppfolgingsplan.arbeidsoppgaveListe),
                        arbeidsoppgave -> new IkkeTattStillingTilArbeidsoppgaveXML()
                                .withNavn(arbeidsoppgave.navn)))
                .withKanIkkeGjennomfoeresArbeidsoppgaveXMLList(mapListe(finnKanIkkeGjennomfoeresArbeidsoppgaver(oppfolgingsplan.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanIkkeGjennomfoeresArbeidsoppgaveXML()
                                .withBeskrivelse(arbeidsoppgave.gjennomfoering.kanIkkeBeskrivelse)
                                .withNavn(arbeidsoppgave.navn)))
                .withKanGjennomfoeresMedTilretteleggingArbeidsoppgaveXMLList(mapListe(finnKanGjennomfoeresMedTilretteleggingArbeidsoppgaver(oppfolgingsplan.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML()
                                .withMedHjelp(arbeidsoppgave.gjennomfoering.medHjelp)
                                .withMedMerTid(arbeidsoppgave.gjennomfoering.medMerTid)
                                .withPaaAnnetSted(arbeidsoppgave.gjennomfoering.paaAnnetSted)
                                .withBeskrivelse(arbeidsoppgave.gjennomfoering.kanBeskrivelse)
                                .withNavn(arbeidsoppgave.navn)))
                .withKanGjennomfoeresArbeidsoppgaveXMLList(mapListe(finnKanGjennomfoeresArbeidsoppgaver(oppfolgingsplan.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanGjennomfoeresArbeidsoppgaveXML()
                                .withNavn(arbeidsoppgave.navn)))
                .withTiltak(mapListe(oppfolgingsplan.tiltakListe, tiltak -> new TiltakXML()
                        .withNavn(tiltak.navn)
                        .withBeskrivelse(tiltak.beskrivelse)
                        .withBeskrivelseIkkeAktuelt(tiltak.beskrivelseIkkeAktuelt)
                        .withStatus(tiltak.status)
                        .withId(tiltak.id)
                        .withGjennomfoering(tiltak.gjennomfoering)
                        .withFom(tilMuntligDatoAarFormat(ofNullable(tiltak.fom).orElse(finnGodkjenning(oppfolgingsplan).gyldighetstidspunkt.fom)))
                        .withTom(tilMuntligDatoAarFormat(ofNullable(tiltak.tom).orElse(finnGodkjenning(oppfolgingsplan).gyldighetstidspunkt.tom)))
                        .withOpprettetAv(brukerprofilService.hentNavnByAktoerId(tiltak.opprettetAvAktoerId))
                ))
                .withStillingListe(mapListe(aaregConsumer.arbeidstakersStillingerForOrgnummer(oppfolgingsplan.arbeidstaker.aktoerId, finnGodkjenning(oppfolgingsplan).gyldighetstidspunkt.fom, oppfolgingsplan.virksomhet.virksomhetsnummer), stilling -> new StillingXML()
                        .withYrke(stilling.yrke)
                        .withProsent(stilling.prosent)))
                .withSykmeldtFnr(sykmeldtFnr)
                .withSykmeldtNavn(sykmeldtnavn)
                .withSykmeldtTlf(sykmeldtKontaktinfo.getMobiltelefonnummer())
                .withSykmeldtEpost(sykmeldtKontaktinfo.getEpostadresse())
                .withVisAdvarsel(false)
                .withFotnote("Oppfølgningsplanen mellom " + sykmeldtnavn + " og " + naermesteleder.navn)
                .withOpprettetAv(!erArbeidstakeren(oppfolgingsplan, innloggetAktoerId) ? sykmeldtnavn : naermesteleder.navn)
                .withOpprettetDato(tilMuntligDatoAarFormat(finnGodkjenning(oppfolgingsplan).godkjenningsTidspunkt.toLocalDate()))
                .withGodkjentAv(erArbeidstakeren(oppfolgingsplan, innloggetAktoerId) ? sykmeldtnavn : naermesteleder.navn)
                .withGodkjentDato(tilMuntligDatoAarFormat(LocalDate.now()))
        );

        String dokumentUuid = UUID.randomUUID().toString();

        boolean skalDeleMedNav = delMedNav || oppfolgingsplan.godkjenninger.stream()
                .anyMatch(godkjenning -> godkjenning.delMedNav);

        godkjentplanDAO.create(new GodkjentPlan()
                .oppfoelgingsdialogId(oppfolgingsplan.id)
                .deltMedNAV(skalDeleMedNav)
                .deltMedNAVTidspunkt((skalDeleMedNav) ? LocalDateTime.now() : null)
                .tvungenGodkjenning(false)
                .dokumentUuid(dokumentUuid)
                .gyldighetstidspunkt(new Gyldighetstidspunkt()
                        .fom(finnGodkjenning(oppfolgingsplan).gyldighetstidspunkt.fom)
                        .tom(finnGodkjenning(oppfolgingsplan).gyldighetstidspunkt.tom)
                        .evalueres(finnGodkjenning(oppfolgingsplan).gyldighetstidspunkt.evalueres)
                ));
        metrikk.tellAntallDagerSiden(oppfolgingsplan.opprettet, "opprettettilgodkjent");
        metrikk.tellAntallDagerSiden(finnGodkjenning(oppfolgingsplan).godkjenningsTidspunkt, "fragodkjenningtilplan");
        dokumentDAO.lagre(new Dokument()
                .uuid(dokumentUuid)
                .pdf(tilPdf(xml))
                .xml(xml)
        );
    }

    public void genererTvungenPlan(Oppfolgingsplan oppfolgingsplan, RSGyldighetstidspunkt gyldighetstidspunkt, boolean delMedNav) {
        rapporterMetrikkerForNyPlan(oppfolgingsplan, true, delMedNav);

        Naermesteleder naermesteleder = narmesteLederConsumer.narmesteLeder(oppfolgingsplan.arbeidstaker.aktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer)
                .orElseThrow(() -> new RuntimeException("Fant ikke nærmeste leder"));
        DigitalKontaktinfo sykmeldtKontaktinfo = dkifConsumer.kontaktinformasjon(oppfolgingsplan.arbeidstaker.aktoerId);
        String sykmeldtnavn = brukerprofilService.hentNavnByAktoerId(oppfolgingsplan.arbeidstaker.aktoerId);
        String sykmeldtFnr = aktorregisterConsumer.hentFnrForAktor(oppfolgingsplan.arbeidstaker.aktoerId);
        String virksomhetsnavn = eregConsumer.virksomhetsnavn(oppfolgingsplan.virksomhet.virksomhetsnummer);
        String xml = JAXB.marshallDialog(new OppfoelgingsdialogXML()
                .withArbeidsgiverEpost(naermesteleder.epost)
                .withArbeidsgivernavn(naermesteleder.navn)
                .withArbeidsgiverOrgnr(oppfolgingsplan.virksomhet.virksomhetsnummer)
                .withVirksomhetsnavn(virksomhetsnavn)
                .withArbeidsgiverTlf(naermesteleder.mobil)
                .withEvalueres(tilMuntligDatoAarFormat(gyldighetstidspunkt.evalueres()))
                .withGyldigfra(tilMuntligDatoAarFormat(gyldighetstidspunkt.fom))
                .withGyldigtil(tilMuntligDatoAarFormat(gyldighetstidspunkt.tom))
                .withSykeforloepsperioderXMLList(mapListe(sykeforloepService.hentSykeforlopperiode(oppfolgingsplan.arbeidstaker.aktoerId, oppfolgingsplan.virksomhet.virksomhetsnummer, EKSTERN), periode -> new SykeforloepsperioderXML()
                        .withFom(tilKortDato(periode.fom))
                        .withTom(tilKortDato(periode.tom))
                        .withAntallDager(antallDagerIPeriode(periode.fom, periode.tom))
                        .withGradering(periode.grad)
                        .withBehandlingsdager(periode.behandlingsdager)
                        .withReisetilskudd(periode.reisetilskudd)
                        .withAvventende(periode.avventende)
                ))
                .withIkkeTattStillingTilArbeidsoppgaveXML(mapListe(finnIkkeTattStillingTilArbeidsoppgaver(oppfolgingsplan.arbeidsoppgaveListe),
                        arbeidsoppgave -> new IkkeTattStillingTilArbeidsoppgaveXML()
                                .withNavn(arbeidsoppgave.navn)))
                .withKanIkkeGjennomfoeresArbeidsoppgaveXMLList(mapListe(finnKanIkkeGjennomfoeresArbeidsoppgaver(oppfolgingsplan.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanIkkeGjennomfoeresArbeidsoppgaveXML()
                                .withBeskrivelse(arbeidsoppgave.gjennomfoering.kanIkkeBeskrivelse)
                                .withNavn(arbeidsoppgave.navn)))
                .withKanGjennomfoeresMedTilretteleggingArbeidsoppgaveXMLList(mapListe(finnKanGjennomfoeresMedTilretteleggingArbeidsoppgaver(oppfolgingsplan.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanGjennomfoeresMedTilretteleggingArbeidsoppgaveXML()
                                .withMedHjelp(arbeidsoppgave.gjennomfoering.medHjelp)
                                .withMedMerTid(arbeidsoppgave.gjennomfoering.medMerTid)
                                .withPaaAnnetSted(arbeidsoppgave.gjennomfoering.paaAnnetSted)
                                .withBeskrivelse(arbeidsoppgave.gjennomfoering.kanBeskrivelse)
                                .withNavn(arbeidsoppgave.navn)))
                .withKanGjennomfoeresArbeidsoppgaveXMLList(mapListe(finnKanGjennomfoeresArbeidsoppgaver(oppfolgingsplan.arbeidsoppgaveListe),
                        arbeidsoppgave -> new KanGjennomfoeresArbeidsoppgaveXML()
                                .withNavn(arbeidsoppgave.navn)))
                .withTiltak(mapListe(oppfolgingsplan.tiltakListe, tiltak -> new TiltakXML()
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
                .withStillingListe(mapListe(aaregConsumer.arbeidstakersStillingerForOrgnummer(oppfolgingsplan.arbeidstaker.aktoerId, gyldighetstidspunkt.fom, oppfolgingsplan.virksomhet.virksomhetsnummer), stilling -> new StillingXML()
                        .withYrke(stilling.yrke)
                        .withProsent(stilling.prosent)))
                .withSykmeldtFnr(sykmeldtFnr)
                .withFotnote("Oppfølgningsplanen mellom " + sykmeldtnavn + " og " + naermesteleder.navn)
                .withSykmeldtNavn(sykmeldtnavn)
                .withSykmeldtTlf(sykmeldtKontaktinfo.getMobiltelefonnummer())
                .withSykmeldtEpost(sykmeldtKontaktinfo.getEpostadresse())
                .withVisAdvarsel(true)
                .withGodkjentAv(naermesteleder.navn)
                .withOpprettetAv(naermesteleder.navn)
                .withOpprettetDato(tilMuntligDatoAarFormat(LocalDate.now()))
                .withGodkjentDato(tilMuntligDatoAarFormat(LocalDate.now()))
        );
        String dokumentUuid = UUID.randomUUID().toString();
        godkjentplanDAO.create(new GodkjentPlan()
                .oppfoelgingsdialogId(oppfolgingsplan.id)
                .deltMedNAV(delMedNav)
                .deltMedNAVTidspunkt(delMedNav ? LocalDateTime.now() : null)
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
    public void avvisGodkjenning(long oppfoelgingsdialogId, String innloggetFnr) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(oppfoelgingsdialogId);
        String innloggetAktoerId = aktorregisterConsumer.hentAktorIdForFnr(innloggetFnr);

        if (!tilgangskontrollService.aktorTilhorerOppfolgingsplan(innloggetAktoerId, oppfolgingsplan)) {
            throw new ForbiddenException("Ikke tilgang");
        }

        if (godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfoelgingsdialogId).size() == 0) {
            throw new ConflictException();
        }
        godkjenningerDAO.deleteAllByOppfoelgingsdialogId(oppfoelgingsdialogId);
        godkjenningerDAO.create(new Godkjenning()
                .godkjent(false)
                .oppfoelgingsdialogId(oppfoelgingsdialogId)
                .godkjentAvAktoerId(innloggetAktoerId)
        );
        oppfolgingsplanDAO.nullstillSamtykke(oppfoelgingsdialogId);
        oppfolgingsplanDAO.sistEndretAv(oppfoelgingsdialogId, innloggetAktoerId);
    }

    private void sendGodkjentPlanTilAltinn(Long oppfoelgingsdialogId) {
        String ressursId = String.valueOf(oppfoelgingsdialogId);
        AsynkOppgave sendOppfoelgingsdialog = asynkOppgaveDAO.create(new AsynkOppgave(OPPFOELGINGSDIALOG_SEND, ressursId));
        asynkOppgaveDAO.create(new AsynkOppgave(OPPFOELGINGSDIALOG_ARKIVER, ressursId, sendOppfoelgingsdialog.id));
    }
}
