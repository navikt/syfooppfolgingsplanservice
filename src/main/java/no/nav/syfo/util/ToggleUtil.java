package no.nav.syfo.util;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.PropertyUtil.ENVIRONMENT_NAME;

public final class ToggleUtil {

    public final static String TOGGLE_SEND_OPPFOLGINGSPLAN_FASTLEGE = "SYFOTOGGLES_SEND_OPPFOELGINGSDIALOG_FASTLEGE";
    public final static String TOGGLE_JURDISK_LOGG = "SYFOTOGGLES_JURIDISKLOGG";
    public final static String TOGGLE_ENABLE_BATCH = "TOGGLE_ENABLE_BATCH";

    private static final String DEFAULT_ON = "true";
    private static final String DEFAULT_OFF = "false";

    public enum ENVIRONMENT_MODE {
        dev,
        p,
        q0,
        q1,
    }

    private static boolean getToggleDefaultOn(String key) {
        return !"false".equals(getProperty(key, DEFAULT_ON));
    }

    private static boolean getToggleDefaultOff(String key) {
        return "true".equals(getProperty(key, DEFAULT_OFF));
    }

    /*
     * SyfoToggles.properties
     * */

    public static boolean toggleBatch() {
        return getToggleDefaultOff(TOGGLE_ENABLE_BATCH);
    }

    public static boolean toggleSendOppfoelgingsdialogFastlege() {
        return getToggleDefaultOff(TOGGLE_SEND_OPPFOLGINGSPLAN_FASTLEGE);
    }

    public static boolean toggleJuridiskLogg() {
        return getToggleDefaultOn(TOGGLE_JURDISK_LOGG);
    }

    /*
     * Miljø-toggles
     * */

    public static boolean kjorerIProduksjon() {
        return getProperty(ENVIRONMENT_NAME, ENVIRONMENT_MODE.p.name()).equalsIgnoreCase(ENVIRONMENT_MODE.p.name()) ||
                getProperty(ENVIRONMENT_NAME).equalsIgnoreCase(ENVIRONMENT_MODE.q0.name());
    }

    public static boolean kjorerLokalt() {
        return getProperty(ENVIRONMENT_NAME, ENVIRONMENT_MODE.p.name()).equalsIgnoreCase(ENVIRONMENT_MODE.dev.name());
    }
}
