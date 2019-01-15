package no.nav.syfo.util;

import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static no.nav.syfo.util.PropertyUtil.ALTINN_TEST_OVERSTYR_ORGNR;
import static no.nav.syfo.util.PropertyUtil.ALTINN_TEST_WHITELIST_ORGNR;

public class MockUtil {
    public static String getOrgnummerForSendingTilAltinn(String orgnummer) {
        return ofNullable(getProperty(ALTINN_TEST_WHITELIST_ORGNR))
                .map(whitelist -> asList(whitelist.split(",")))
                .map(whitelist -> whitelist.contains(orgnummer))
                .filter(Boolean::booleanValue)
                .map(b -> orgnummer)
                .orElseGet(() -> getProperty(ALTINN_TEST_OVERSTYR_ORGNR, orgnummer));
    }
}
