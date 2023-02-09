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

    @Inject
    private LeaderElectionService leaderElectionService;

    @Inject
    private PdlConsumer pdlConsumer;

    @Inject
    private OppfolgingsplanDAO oppfolgingsplanDAO;

    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        if (leaderElectionService.isLeader()) {
            LOG.info("Running aktorid migration ... ");
            List<POppfoelgingsdialog> rowsToMigrate = oppfolgingsplanDAO.plansWithoutFnr();
            LOG.info("# rows to migrate: " + rowsToMigrate.size());
            rowsToMigrate.forEach(this::migrateRow);
        }
    }

    private void migrateRow(POppfoelgingsdialog row) {
        String smFnr = pdlConsumer.fnr(row.aktoerId);
        String opprettetAvFnr = pdlConsumer.fnr(row.opprettetAv);
        String sistEndretAvFnr = pdlConsumer.fnr(row.sistEndretAv);

        if (oppfolgingsplanDAO.updateSmFnr(row.id, smFnr))
            LOG.error("ERROR: Could not update sm_fnr in row " + row.id);

        if (oppfolgingsplanDAO.updateOpprettetAvFnr(row.id, opprettetAvFnr))
            LOG.error("ERROR: Could not update opprettet_av_fnr in row " + row.id);

        if (oppfolgingsplanDAO.updateSistEndretAvFnr(row.id, sistEndretAvFnr))
            LOG.error("ERROR: Could not update sist_endret_av_fnr in row " + row.id);
    }

}
