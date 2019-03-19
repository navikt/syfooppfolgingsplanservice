package no.nav.syfo.util;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.System.getProperty;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
public final class OppfoelgingsdialogservicePropertiesUtil {

    public static int hentIntProperty(String property, int defaultValue) {
        try {
            return ofNullable(getProperty(property))
                    .map(Integer::parseInt)
                    .orElse(defaultValue);
        } catch (Exception e) {
            log.error("Feil ved henting av Int-property " + property, e);
            throw e;
        }
    }

    public static <T extends Enum<T>> List<T> getPropertyEnumListe(String property, Class<T> clazz) {
        return getPropertyStream(property)
                .map(propertyStream -> propertyStream
                        .map(e -> Enum.valueOf(clazz, e))
                        .collect(toList()))
                .orElse(emptyList());
    }

    public static List<String> getPropertyListe(String property) {
        return getPropertyStream(property)
                .map(propertyStream -> propertyStream
                        .collect(toList()))
                .orElse(emptyList());
    }

    private static Optional<Stream<String>> getPropertyStream(String property) {
        return ofNullable(getProperty(property))
                .filter(s -> !s.isEmpty())
                .map(propertyList -> propertyList.split(","))
                .map(Stream::of)
                .map(stringStream -> stringStream
                        .map(String::trim)
                        .filter(s -> !s.isEmpty()));
    }
}
