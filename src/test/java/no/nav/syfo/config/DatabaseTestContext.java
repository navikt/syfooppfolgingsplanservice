package no.nav.syfo.config;

import lombok.val;
import no.nav.dialogarena.config.fasit.DbCredentials;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.TestEnvironment;

import java.util.Optional;

import static no.nav.syfo.config.ApplicationConfig.APPLICATION_NAME;
import static no.nav.syfo.config.DatabaseConfig.*;

public class DatabaseTestContext {

    public static void setupContext(String miljo) {
        val dbCredential = Optional.ofNullable(miljo)
                .map(TestEnvironment::valueOf)
                .map(testEnvironment -> FasitUtils.getDbCredentials(testEnvironment, APPLICATION_NAME));

        if (dbCredential.isPresent()) {
            setDataSourceProperties(dbCredential.get());
        } else {
            setInMemoryDataSourceProperties();
        }

    }

    public static void setupInMemoryContext() {
        setupContext(null);
    }

    private static void setDataSourceProperties(DbCredentials dbCredentials) {
        System.setProperty(SERVICEOPPFOELGINGSDIALOGDB_URL, dbCredentials.url);
        System.setProperty(SERVICEOPPFOELGINGSDIALOGDB_USERNAME, dbCredentials.getUsername());
        System.setProperty(SERVICEOPPFOELGINGSDIALOGDB_PASSWORD, dbCredentials.getPassword());

    }

    private static void setInMemoryDataSourceProperties() {
        System.setProperty(SERVICEOPPFOELGINGSDIALOGDB_URL,
                "jdbc:h2:mem:syfooppfolgingsplanservice;DB_CLOSE_DELAY=-1;MODE=Oracle");
        System.setProperty(SERVICEOPPFOELGINGSDIALOGDB_USERNAME, "sa");
        System.setProperty(SERVICEOPPFOELGINGSDIALOGDB_PASSWORD, "password");
    }
}
