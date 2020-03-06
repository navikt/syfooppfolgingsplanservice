package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.dokarkiv.DokArkivConsumer;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.Person;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;


@Service
public class JournalforOPService {

    private AktorregisterConsumer aktorregisterConsumer;
    private OppfoelingsdialogDAO oppfoelingsdialogDAO; //Todo fiks skrivefeil i OppfoelingsdialogDAO
    private OrganisasjonService organisasjonService;
    private DokArkivConsumer dokArkivConsumer;
    private BrukerprofilService brukerprofilService;
    private DokumentService dokumentService;


    @Inject
    public JournalforOPService(
            AktorregisterConsumer aktorregisterConsumer,
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            OrganisasjonService organisasjonService,
            DokArkivConsumer dokArkivConsumer,
            BrukerprofilService brukerprofilService,
            DokumentService dokumentService
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.organisasjonService = organisasjonService;
        this.dokArkivConsumer = dokArkivConsumer;
        this.brukerprofilService = brukerprofilService;
        this.dokumentService = dokumentService;
    }

    public Integer opprettJournalpost(GodkjentPlan godkjentPlan) {
        Oppfoelgingsdialog oppfolgingsplan = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(godkjentPlan.oppfoelgingsdialogId);
        String virksomhetsnavn = organisasjonService.finnVirksomhetsnavn(oppfolgingsplan.virksomhet.virksomhetsnummer);
        oppfolgingsplan.virksomhet.navn(virksomhetsnavn);
        setArbeidstakerinfo(oppfolgingsplan);
        setPDF(godkjentPlan);

        return dokArkivConsumer.journalforOppfolgingsplan(oppfolgingsplan, godkjentPlan);
    }

    private void setPDF(GodkjentPlan godkjentPlan) {
        godkjentPlan.dokument = dokumentService.hentDokument(godkjentPlan.dokumentUuid);
    }

    private void setArbeidstakerinfo(Oppfoelgingsdialog oppfoelgingsplan) {
        Person person = brukerprofilService.hentNavnOgFnr(oppfoelgingsplan.arbeidstaker.aktoerId);
        oppfoelgingsplan.arbeidstaker.navn(person.navn);
        oppfoelgingsplan.arbeidstaker.fnr(person.fnr);
    }
}


