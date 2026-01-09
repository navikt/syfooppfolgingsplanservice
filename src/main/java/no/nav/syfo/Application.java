package no.nav.syfo;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

@SpringBootApplication(exclude = {
        FlywayAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@EnableJwtTokenValidation
//@ConfigurationPropertiesScan
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .initializers(applicationContext -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("ORACLE_DB_URL", fromFile("/secrets/oppfolgingsplandb/config/jdbc_url"));
                    properties.put("ORACLE_DB_USERNAME", fromFile("/secrets/oppfolgingsplandb/credentials/username"));
                    properties.put("ORACLE_DB_PASSWORD", fromFile("/secrets/oppfolgingsplandb/credentials/password"));
                    var propertySource = new MapPropertySource("database-properties", unmodifiableMap(properties));
                    applicationContext.getEnvironment().getPropertySources().addFirst(propertySource);
                })
                .run(args);
    }

    private static String fromFile(String filename) {
        try {
            Path file = Paths.get(filename);
            List<String> lines = Files.readAllLines(file);
            return lines.getFirst();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to read property value from " + filename, exception);
        }
    }
}
