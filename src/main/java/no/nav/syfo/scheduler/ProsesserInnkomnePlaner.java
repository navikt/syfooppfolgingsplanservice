package no.nav.syfo.scheduler;

import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.service.AktoerService;
import no.nav.syfo.service.BehandleSakService;
import no.nav.syfo.service.JournalService;
import no.nav.syfo.service.SakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;
import static no.nav.syfo.util.ToggleUtil.toggleBatch;

public class ProsesserInnkomnePlaner {

    public static final Logger LOG = LoggerFactory.getLogger(ProsesserInnkomnePlaner.class);

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
            LOG.info("TRACEBATCH: run {}", this.getClass().getName());

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
            LOG.info("TRACEBATCH: run {}", this.getClass().getName());

            godkjentplanDAO.hentIkkeJournalfoertePlaner()
                    .forEach(godkjentPlan -> godkjentplanDAO.journalpostId(godkjentPlan.oppfoelgingsdialogId, journalService.opprettJournalpost(godkjentPlan.sakId, godkjentPlan))
                    );
        }
    }
}
