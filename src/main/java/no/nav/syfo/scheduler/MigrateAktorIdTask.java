package no.nav.syfo.scheduler;

import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.repository.dao.OppfolgingsplanDAO;
import no.nav.syfo.repository.domain.POppfoelgingsdialog;
import no.nav.syfo.service.LeaderElectionService;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class MigrateAktorIdTask {

    private static final Logger LOG = getLogger(MigrateAktorIdTask.class);
    private static final int BATCH_SIZE = 20000;

    @Inject
    private LeaderElectionService leaderElectionService;

    @Inject
    private PdlConsumer pdlConsumer;

    @Inject
    private OppfolgingsplanDAO oppfolgingsplanDAO;

    @Scheduled(cron = "0 0/20 21-6 * * *")
    public void run() {
        if (leaderElectionService.isLeader()) {
            LOG.info("Running aktorid migration ... ");
            List<POppfoelgingsdialog> rowsToMigrate = oppfolgingsplanDAO.plansWithoutFnr(BATCH_SIZE);
            LOG.info("# rows to migrate: " + rowsToMigrate.size());
            rowsToMigrate.forEach(this::migrateRow);
            LOG.info("Partial migration finished");
        }
    }

    private void migrateRow(POppfoelgingsdialog row) {
        String smFnr = getFnrForRowIfPresent(row.aktoerId, row.id);
        String opprettetAvFnr = getFnrForRowIfPresent(row.opprettetAv, row.id);
        String sistEndretAvFnr = getFnrForRowIfPresent(row.sistEndretAv, row.id);

        if (smFnr == null || !oppfolgingsplanDAO.updateSmFnr(row.id, smFnr))
            LOG.error("ERROR: Could not update sm_fnr in row " + row.id);

        if (opprettetAvFnr == null || !oppfolgingsplanDAO.updateOpprettetAvFnr(row.id, opprettetAvFnr))
            LOG.error("ERROR: Could not update opprettet_av_fnr in row " + row.id);

        if (sistEndretAvFnr == null || !oppfolgingsplanDAO.updateSistEndretAvFnr(row.id, sistEndretAvFnr))
            LOG.error("ERROR: Could not update sist_endret_av_fnr in row " + row.id);
    }

    public String getFnrForRowIfPresent(String aktorid, Long rowNum) {
        try {
            return pdlConsumer.fnr(aktorid);
        } catch (RuntimeException e) {
            LOG.error("Could not migrate row " + rowNum + " unable to exchange aktorid for fnr");
        }
        return null;
    }

}
