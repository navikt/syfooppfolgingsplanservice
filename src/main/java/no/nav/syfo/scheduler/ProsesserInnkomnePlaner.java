package no.nav.syfo.scheduler;

import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.service.JournalforOPService;
import no.nav.syfo.service.LeaderElectionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class ProsesserInnkomnePlaner {
    private GodkjentplanDAO godkjentplanDAO;
    private JournalforOPService journalforOPService;
    private LeaderElectionService leaderElectionService;
    private final Metrikk metrikk;

    @Inject
    public ProsesserInnkomnePlaner(
            GodkjentplanDAO godkjentplanDAO,
            JournalforOPService journalforOPService,
            LeaderElectionService leaderElectionService,
            Metrikk metrikk
    ) {
        this.godkjentplanDAO = godkjentplanDAO;
        this.journalforOPService = journalforOPService;
        this.leaderElectionService = leaderElectionService;
        this.metrikk = metrikk;
    }


    @Scheduled(fixedRate = 60000)
    public void opprettJournalposter() {
        if (leaderElectionService.isLeader()) {
            godkjentplanDAO.hentIkkeJournalfoertePlaner()
                    .forEach(godkjentPlan -> {
                        godkjentplanDAO.journalpostId(
                                godkjentPlan.oppfoelgingsdialogId,
                                journalforOPService.opprettJournalpost(godkjentPlan).toString());
                        metrikk.tellHendelse("plan_opprettet_journal_gosys");
                    });
        }
    }
}
