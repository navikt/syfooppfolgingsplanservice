package no.nav.syfo.util;

import static java.lang.System.getenv;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static no.nav.syfo.util.PropertyUtil.ALTINN_TEST_WHITELIST_ORGNR;

public class MockUtil {

    public static String getOrgnummerForSendingTilAltinn(String orgnummer) {

        return ofNullable(getenv(ALTINN_TEST_WHITELIST_ORGNR))
                .map(whitelist -> asList(whitelist.split(",")))
                .map(whitelist -> whitelist.contains(orgnummer) ? orgnummer : whitelist.get(0))
                .orElse(orgnummer);
    }
}
