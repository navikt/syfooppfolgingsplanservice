package no.nav.syfo.lps.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*
import javax.inject.Inject

@Component
class MigrationLpsProducer @Inject constructor(
    private val lpsMigrationKafkaTemplate: KafkaTemplate<String, AltinnLpsOppfolgingsplan>,
) {
    fun migrateAltinnLpsPlan(altinnLpsOppfolgingsplan: AltinnLpsOppfolgingsplan): UUID? {
        val uuid = altinnLpsOppfolgingsplan.uuid
        return try {
            lpsMigrationKafkaTemplate.send(
                    MIGRATE_LPS_TOPIC,
                    UUID.randomUUID().toString(),
                    altinnLpsOppfolgingsplan,
            ).get()
            log.info("Sent LPS-OP with uuid to migration topic: $uuid")
            uuid
        } catch (e: Exception) {
            log.error("Failed while attempting to migrate LPS-OP with uuid $uuid", e)
            null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(MigrationLpsProducer::class.java)
        const val MIGRATE_LPS_TOPIC = "team-esyfo.syfo-migrering-altinn-lps"
    }
}
