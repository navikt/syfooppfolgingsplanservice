package no.nav.syfo.util;

import org.springframework.core.env.Environment;

import static java.lang.System.getenv;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static no.nav.syfo.util.PropertyUtil.ALTINN_TEST_WHITELIST_ORGNR;

public class MockUtil {

    public MockUtil(Environment springEnv) {
        springEnv.getRequiredProperty("ldap.username");
    }

    public static String getOrgnummerForSendingTilAltinn(String orgnummer) {
        return ofNullable(getenv(ALTINN_TEST_WHITELIST_ORGNR))
                .map(whitelist -> asList(whitelist.split(",")))
                .map(whitelist -> whitelist.contains(orgnummer))
                .filter(Boolean::booleanValue)
                .map(b -> orgnummer)
                .orElse(ofNullable(getenv(ALTINN_TEST_WHITELIST_ORGNR)).orElse(orgnummer));
    }
}
