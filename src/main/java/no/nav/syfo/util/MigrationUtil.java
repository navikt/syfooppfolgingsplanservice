package no.nav.syfo.util;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class MigrationUtil {
    public static void createTables(DataSource dataSource) {
        Flyway flyway = new Flyway();
        flyway.setValidateOnMigrate(false);
        flyway.setDataSource(dataSource);
        flyway.migrate();
    }
}
