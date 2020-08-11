package no.nav.syfo.service;

import no.nav.syfo.dokarkiv.DokArkivConsumer;
import no.nav.syfo.domain.*;
import no.nav.syfo.ereg.EregConsumer;
import no.nav.syfo.lps.OppfolgingsplanLPS;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.OppfolgingsplanDAO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;


@Service
public class JournalforOPService {

    private OppfolgingsplanDAO oppfolgingsplanDAO;
    private DokArkivConsumer dokArkivConsumer;
    private EregConsumer eregConsumer;
    private BrukerprofilService brukerprofilService;
    private DokumentService dokumentService;
    private final PdlConsumer pdlConsumer;


    @Inject
    public JournalforOPService(
            OppfolgingsplanDAO oppfolgingsplanDAO,
            DokArkivConsumer dokArkivConsumer,
            EregConsumer eregConsumer,
            BrukerprofilService brukerprofilService,
            DokumentService dokumentService,
            PdlConsumer pdlConsumer
    ) {
        this.oppfolgingsplanDAO = oppfolgingsplanDAO;
        this.dokArkivConsumer = dokArkivConsumer;
        this.eregConsumer = eregConsumer;
        this.brukerprofilService = brukerprofilService;
        this.dokumentService = dokumentService;
        this.pdlConsumer = pdlConsumer;
    }

    public Integer opprettJournalpost(GodkjentPlan godkjentPlan) {
        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.finnOppfolgingsplanMedId(godkjentPlan.oppfoelgingsdialogId);
        String virksomhetsnavn = eregConsumer.virksomhetsnavn(oppfolgingsplan.virksomhet.virksomhetsnummer);
        oppfolgingsplan.virksomhet.navn(virksomhetsnavn);
        setArbeidstakerinfo(oppfolgingsplan);
        setPDF(godkjentPlan);

        return dokArkivConsumer.journalforOppfolgingsplan(oppfolgingsplan, godkjentPlan);
    }

    public Integer createJournalpostPlanLPS(OppfolgingsplanLPS oppfolgingsplanLPS) {
        String virksomhetsnavn = eregConsumer.virksomhetsnavn(oppfolgingsplanLPS.getVirksomhetsnummer());
        String arbeidstakerNavn = pdlConsumer.personName(oppfolgingsplanLPS.getFnr());

        return dokArkivConsumer.journalforOppfolgingsplanLPS(
                virksomhetsnavn,
                oppfolgingsplanLPS.getFnr(),
                arbeidstakerNavn,
                oppfolgingsplanLPS.getPdf()
        );
    }

    private void setPDF(GodkjentPlan godkjentPlan) {
        godkjentPlan.dokument = dokumentService.hentDokument(godkjentPlan.dokumentUuid);
    }

    private void setArbeidstakerinfo(Oppfolgingsplan oppfoelgingsplan) {
        Person person = brukerprofilService.hentNavnOgFnr(oppfoelgingsplan.arbeidstaker.aktoerId);
        oppfoelgingsplan.arbeidstaker.navn(person.navn);
        oppfoelgingsplan.arbeidstaker.fnr(person.fnr);
    }
}


