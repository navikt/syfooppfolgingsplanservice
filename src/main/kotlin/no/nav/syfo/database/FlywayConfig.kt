package no.nav.syfo.database

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.transaction.jta.JtaTransactionManager
import javax.sql.DataSource

@Profile("remote")
@Configuration
class FlywayConfig() {

    @Bean
    fun flyway(dataSource: DataSource): Flyway = Flyway
        .configure()
        .dataSource(dataSource)
        .validateOnMigrate(false)
        .table("schema_version")
        .load()

    @Bean
    fun flywayMigrationStrategy(jtaTransactionManager: JtaTransactionManager) =
        FlywayMigrationStrategy { flyway ->
            flyway.repair()
            flyway.migrate()
        }

    @Bean
    fun flywayMigrationInitializer(flyway: Flyway, flywayMigrationStrategy: FlywayMigrationStrategy): FlywayMigrationInitializer =
        FlywayMigrationInitializer(flyway, flywayMigrationStrategy)
}
