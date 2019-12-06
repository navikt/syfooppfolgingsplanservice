package no.nav.syfo.util;

import java.util.Base64;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static no.nav.syfo.util.PropertyUtil.NAIS_CLUSTER_NAME;

public class RestUtils {

    public static String baseUrl() {
        return "https://app" + miljo() + ".adeo.no";
    }

    private static String miljo() {
        String environmentName = getenv(NAIS_CLUSTER_NAME);
        if ("dev-fss".equals(environmentName)) {
            return "-q1";
        }
        return "";

    }

    public static String basicCredentials(String credentialUsername, String credentialPassword) {
        return "Basic " + Base64.getEncoder().encodeToString(format("%s:%s", credentialUsername, credentialPassword).getBytes());
    }

    public static String bearerHeader(String token) {
        return "Bearer " + token;
    }
}
