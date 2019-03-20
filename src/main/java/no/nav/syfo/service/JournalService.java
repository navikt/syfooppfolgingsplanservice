package no.nav.syfo.service;

import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.*;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.*;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Service
public class JournalService {

    private static final String GOSYS = "FS22";

    private AktoerService aktoerService;
    private BehandleJournalV2 behandleJournalV2;
    private BrukerprofilService brukerprofilService;
    private DokumentService dokumentService;
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    private OrganisasjonService organisasjonService;

    @Inject
    public JournalService(
            AktoerService aktoerService,
            BehandleJournalV2 behandleJournalV2,
            BrukerprofilService brukerprofilService,
            DokumentService dokumentService,
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            OrganisasjonService organisasjonService
    ) {
        this.aktoerService = aktoerService;
        this.behandleJournalV2 = behandleJournalV2;
        this.brukerprofilService = brukerprofilService;
        this.dokumentService = dokumentService;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.organisasjonService = organisasjonService;
    }

    public String opprettJournalpost(String saksId, GodkjentPlan godkjentPlan) {
        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(godkjentPlan.oppfoelgingsdialogId);
        String virksomhetsnavn = organisasjonService.finnVirksomhetsnavn(oppfoelgingsdialog.virksomhet.virksomhetsnummer);
        String dokumentNavn = format("Oppfølgingsplan %s", virksomhetsnavn);
        String fnr = aktoerService.hentFnrForAktoer(oppfoelgingsdialog.sistEndretAvAktoerId);

        return behandleJournalV2.journalfoerInngaaendeHenvendelse(
                new WSJournalfoerInngaaendeHenvendelseRequest()
                        .withApplikasjonsID("SRVOPPFOELGINGSDIAL")
                        .withJournalpost(new WSJournalpost()
                                .withDokumentDato(LocalDateTime.now())
                                .withJournalfoerendeEnhetREF(GOSYS)
                                .withKanal(new WSKommunikasjonskanaler().withValue("NAV_NO"))
                                .withSignatur(new WSSignatur().withSignert(true))
                                .withArkivtema(new WSArkivtemaer().withValue("OPP"))
                                .withForBruker(new WSPerson().withIdent(new WSNorskIdent().withIdent(fnr)))
                                .withOpprettetAvNavn("Serviceoppfoelgingsdialog")
                                .withInnhold(dokumentNavn)
                                .withEksternPart(new WSEksternPart()
                                        .withNavn(sykmeldtAvsendernavnHvisSistEndretAvSykmeldt(oppfoelgingsdialog).orElse(virksomhetsnavn))
                                        .withEksternAktoer(new WSPerson().withIdent(new WSNorskIdent().withIdent(fnr))))
                                .withGjelderSak(new WSSak().withSaksId(saksId).withFagsystemkode(GOSYS))
                                .withMottattDato(LocalDateTime.now())
                                .withDokumentinfoRelasjon(
                                        new WSDokumentinfoRelasjon()
                                                .withTillknyttetJournalpostSomKode("HOVEDDOKUMENT")
                                                .withJournalfoertDokument(new WSJournalfoertDokumentInfo()
                                                        .withBegrensetPartsInnsyn(false)
                                                        .withDokumentType(new WSDokumenttyper().withValue("ES"))
                                                        .withSensitivitet(true)
                                                        .withTittel(dokumentNavn)
                                                        .withKategorikode("ES")
                                                        .withBeskriverInnhold(
                                                                new WSStrukturertInnhold()
                                                                        .withFilnavn(dokumentNavn)
                                                                        .withFiltype(new WSArkivfiltyper().withValue("PDF"))
                                                                        .withInnhold(dokumentService.hentDokument(godkjentPlan.dokumentUuid))
                                                                        .withVariantformat(new WSVariantformater().withValue("ARKIV"))
                                                        ))

                                ))
        ).getJournalpostId();
    }

    private Optional<String> sykmeldtAvsendernavnHvisSistEndretAvSykmeldt(Oppfoelgingsdialog oppfoelgingsdialog) {
        if (oppfoelgingsdialog.sistEndretAvAktoerId.equals(oppfoelgingsdialog.arbeidstaker.aktoerId)) {
            return of(brukerprofilService.hentNavnByAktoerId(oppfoelgingsdialog.arbeidstaker.aktoerId));
        }
        return empty();
    }
}
