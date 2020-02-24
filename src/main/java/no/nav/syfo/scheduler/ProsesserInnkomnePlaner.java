package no.nav.syfo.scheduler;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.service.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.PropertyUtil.LOCAL_MOCK;

@Slf4j
@Service
public class ProsesserInnkomnePlaner {
    private AktorregisterConsumer aktorregisterConsumer;
    private BehandleSakService behandleSakService;
    private GodkjentplanDAO godkjentplanDAO;
    private JournalService journalService;
    private LeaderElectionService leaderElectionService;
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    private SakService sakService;
    private final Metrikk metrikk;

    @Inject
    public ProsesserInnkomnePlaner(
            AktorregisterConsumer aktorregisterConsumer,
            BehandleSakService behandleSakService,
            GodkjentplanDAO godkjentplanDAO,
            JournalService journalService,
            LeaderElectionService leaderElectionService,
            OppfoelingsdialogDAO oppfoelingsdialogDAO,
            SakService sakService,
            Metrikk metrikk
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.behandleSakService = behandleSakService;
        this.godkjentplanDAO = godkjentplanDAO;
        this.journalService = journalService;
        this.leaderElectionService = leaderElectionService;
        this.oppfoelingsdialogDAO = oppfoelingsdialogDAO;
        this.sakService = sakService;
        this.metrikk = metrikk;
    }

    @Scheduled(fixedRate = 60000)
    public void opprettSaker() {
        if (leaderElectionService.isLeader()) {
            godkjentplanDAO.hentIkkeSaksfoertePlaner()
                    .forEach(godkjentPlan -> {
                        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.finnOppfolgingsplanMedId(godkjentPlan.oppfoelgingsdialogId);
                        String fnr = aktorregisterConsumer.hentFnrForAktor(oppfoelgingsdialog.arbeidstaker.aktoerId);
                        String sakId = sakService.finnSak(fnr).orElse(behandleSakService.opprettSak(fnr));
                        godkjentplanDAO.sakId(oppfoelgingsdialog.id, sakId);
                        metrikk.tellHendelse("plan_opprettet_sak_gosys");
                    });
        }
    }

    @Scheduled(fixedRate = 60000)
    public void opprettJournalposter() {
        if (leaderElectionService.isLeader() && !"true".equals(getProperty(LOCAL_MOCK))) {
            godkjentplanDAO.hentIkkeJournalfoertePlaner()
                    .forEach(godkjentPlan -> {
                        godkjentplanDAO.journalpostId(godkjentPlan.oppfoelgingsdialogId, journalService.opprettJournalpost(godkjentPlan.sakId, godkjentPlan));
                        metrikk.tellHendelse("plan_opprettet_journal_gosys");
                    });
        }
    }
}
