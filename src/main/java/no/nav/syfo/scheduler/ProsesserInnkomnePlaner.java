package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.service.*;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static no.nav.syfo.util.ToggleUtil.toggleBatch;

@Slf4j
public class ProsesserInnkomnePlaner {

    @Inject
    private BehandleSakService behandleSakService;
    @Inject
    private SakService sakService;
    @Inject
    private AktoerService aktoerService;
    @Inject
    private GodkjentplanDAO godkjentplanDAO;
    @Inject
    private JournalService journalService;
    @Inject
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;

    @Scheduled(fixedRate = 60000)
    public void opprettSaker() {
        if (!"true".equals(getProperty(LOCAL_MOCK)) && toggleBatch()) {
            log.info("TRACEBATCH: run {}", this.getClass().getName());

            godkjentplanDAO.hentIkkeSaksfoertePlaner()
                    .forEach(godkjentPlan -> {
                        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(godkjentPlan.oppfoelgingsdialogId);
                        String fnr = aktoerService.hentFnrForAktoer(oppfoelgingsdialog.arbeidstaker.aktoerId);
                        String sakId = sakService.finnSak(fnr).orElse(behandleSakService.opprettSak(fnr));
                        godkjentplanDAO.sakId(oppfoelgingsdialog.id, sakId);
                    });
        }
    }

    @Scheduled(fixedRate = 60000)
    public void opprettJournalposter() {
        if (!"true".equals(getProperty(LOCAL_MOCK)) && toggleBatch()) {
            log.info("TRACEBATCH: run {}", this.getClass().getName());

            godkjentplanDAO.hentIkkeJournalfoertePlaner()
                    .forEach(godkjentPlan -> godkjentplanDAO.journalpostId(godkjentPlan.oppfoelgingsdialogId, journalService.opprettJournalpost(godkjentPlan.sakId, godkjentPlan))
                    );
        }
    }
}
