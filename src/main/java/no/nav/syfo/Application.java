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
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;

@SpringBootApplication(exclude = {
        FlywayAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@EnableJwtTokenValidation
@ConfigurationPropertiesScan
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .initializers(applicationContext -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("ORACLE_DB_URL", fromFile("/secrets/oppfolgingsplandb/config/jdbc_url"));
                    properties.put("ORACLE_DB_USERNAME", fromFile("/secrets/oppfolgingsplandb/credentials/username"));
                    properties.put("ORACLE_DB_PASSWORD", fromFile("/secrets/oppfolgingsplandb/credentials/password"));
                    properties.putAll(naisVault());
                    var propertySource = new MapPropertySource("database-properties", unmodifiableMap(properties));
                    applicationContext.getEnvironment().getPropertySources().addFirst(propertySource);
                })
                .run(args);
    }

    private static List<String> fromFile(String filename) {
        try {
            Path file = Paths.get(filename);
            return Files.readAllLines(file);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to read lines from " + filename, exception);
        }
    }

    private static Map<String, Object> naisVault() {
        Map<String, Object> properties = new HashMap<>();

        try (Stream<Path> stream = Files.list(Paths.get("/var/run/secrets/nais.io/vault"))) {
            List<String> fileList = stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toString)
                    .toList();

            fileList.forEach(filePath -> {
                List<String> secrets = fromFile(filePath);
                secrets.forEach(secret -> {
                    String[] parts = secret.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim().replaceAll("^\"|\"$", "");
                        properties.put(key, value);
                    }
                });
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to get list of files from vault", e);
        }
        return properties;
    }
}
