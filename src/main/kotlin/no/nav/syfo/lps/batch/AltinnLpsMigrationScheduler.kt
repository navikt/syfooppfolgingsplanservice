package no.nav.syfo.lps.batch

import no.nav.syfo.lps.database.OppfolgingsplanLPSDAO
import no.nav.syfo.lps.kafka.AltinnLpsOppfolgingsplan
import no.nav.syfo.lps.kafka.MigrationLpsProducer
import no.nav.syfo.service.LeaderElectionService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.inject.Inject

const val FIFTEEN_MINUTES_MILLISECONDS: Long = 7 * 60 * 1000
const val BATCH_SIZE = 1000

@Service
class AltinnLpsMigrationScheduler @Inject constructor(
    private val oppfolgingsplanLpsDao: OppfolgingsplanLPSDAO,
    private val migrationLpsProducer: MigrationLpsProducer,
    private var leaderElectionService: LeaderElectionService
) {
    @Scheduled(fixedRate = FIFTEEN_MINUTES_MILLISECONDS)
    fun migrateAltinnLpsPlans() {
        if (leaderElectionService.isLeader()) {
            val altinnLpsPlansToMigrate = oppfolgingsplanLpsDao.getPlansNotYetMigrated(BATCH_SIZE)
            log.info("Attempting to migrate " + altinnLpsPlansToMigrate.size + " LPS plans")

            altinnLpsPlansToMigrate.forEach { lps ->
                val altinnLpsOppfolgingsplan = AltinnLpsOppfolgingsplan(
                    archiveReference = lps.archiveReference,
                    uuid = lps.uuid,
                    lpsFnr = lps.fnr,
                    fnr = lps.fnr,
                    orgnummer = lps.virksomhetsnummer,
                    pdf = lps.pdf,
                    xml = lps.xml,
                    shouldSendToNav = lps.deltMedNav,
                    shouldSendToFastlege = lps.delMedFastlege,
                    sentToNav = lps.deltMedNav,
                    sentToFastlege = lps.deltMedFastlege,
                    sendToFastlegeRetryCount = 0,
                    journalpostId = lps.journalpostId,
                    created = lps.opprettet,
                    lastChanged = lps.sistEndret,
                )
                val migratedPlanUuid = migrationLpsProducer.migrateAltinnLpsPlan(altinnLpsOppfolgingsplan)
                migratedPlanUuid?.let { oppfolgingsplanLpsDao.updateMigrationStatus(it) }

                log.info("Migrated " + altinnLpsPlansToMigrate.size + " LPS plans")
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AltinnLpsMigrationScheduler::class.java)
    }
}
