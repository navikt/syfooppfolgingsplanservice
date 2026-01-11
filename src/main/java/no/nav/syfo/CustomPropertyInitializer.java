package no.nav.syfo;


import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CustomPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("ORACLE_DB_URL", fromFile("/secrets/oppfolgingsplandb/config/jdbc_url").getFirst());
        properties.put("ORACLE_DB_USERNAME", fromFile("/secrets/oppfolgingsplandb/credentials/username").getFirst());
        properties.put("ORACLE_DB_PASSWORD", fromFile("/secrets/oppfolgingsplandb/credentials/password").getFirst());
        properties.putAll(naisVault());
        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("myCustomFileSource", properties)
        );
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
                        String key = parts[0].trim().replaceAll("_", ".").toLowerCase();
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
