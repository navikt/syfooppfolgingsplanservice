package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.service.*;
import no.nav.syfo.util.Toggle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;

@Slf4j
@Service
public class ProsesserInnkomnePlaner {
    private AktoerService aktoerService;
    private BehandleSakService behandleSakService;
    private GodkjentplanDAO godkjentplanDAO;
    private JournalService journalService;
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    private SakService sakService;
    private final Metrikk metrikk;
    private Toggle toggle;

    @Inject
    public ProsesserInnkomnePlaner(
            AktoerService aktoerService,
            BehandleSakService behandleSakService,
            GodkjentplanDAO godkjentplanDAO,
            JournalService journalService,
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            SakService sakService,
            Metrikk metrikk,
            Toggle toggle
    ) {
        this.aktoerService = aktoerService;
        this.behandleSakService = behandleSakService;
        this.godkjentplanDAO = godkjentplanDAO;
        this.journalService = journalService;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.sakService = sakService;
        this.metrikk = metrikk;
        this.toggle = toggle;
    }

    @Scheduled(fixedRate = 60000)
    public void opprettSaker() {
        if (toggle.toggleBatch()) {
            log.info("TRACEBATCH: run {} opprettSaker", this.getClass().getName());

            godkjentplanDAO.hentIkkeSaksfoertePlaner()
                    .forEach(godkjentPlan -> {
                        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfoelgingsdialogMedId(godkjentPlan.oppfoelgingsdialogId);
                        String fnr = aktoerService.hentFnrForAktoer(oppfoelgingsdialog.arbeidstaker.aktoerId);
                        String sakId = sakService.finnSak(fnr).orElse(behandleSakService.opprettSak(fnr));
                        godkjentplanDAO.sakId(oppfoelgingsdialog.id, sakId);
                        metrikk.tellHendelse("plan_opprettet_sak_gosys");
                    });
        }
    }

    @Scheduled(fixedRate = 60000)
    public void opprettJournalposter() {
        if (!"true".equals(getProperty(LOCAL_MOCK)) && toggle.toggleBatch()) {
            log.info("TRACEBATCH: run {} opprettJournalposter", this.getClass().getName());

            godkjentplanDAO.hentIkkeJournalfoertePlaner()
                    .forEach(godkjentPlan -> {
                        godkjentplanDAO.journalpostId(godkjentPlan.oppfoelgingsdialogId, journalService.opprettJournalpost(godkjentPlan.sakId, godkjentPlan));
                        metrikk.tellHendelse("plan_opprettet_journal_gosys");
                    });
        }
    }
}
