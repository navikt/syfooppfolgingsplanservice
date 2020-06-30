package no.nav.syfo.util;

import static java.lang.System.getProperty;
import static no.nav.syfo.util.PropertyUtil.ENVIRONMENT_NAME;

public final class ToggleUtil {

    public enum ENVIRONMENT_MODE {
        dev,
        p,
        q0,
        q1,
    }

    public static boolean kjorerLokalt() {
        return getProperty(ENVIRONMENT_NAME, ENVIRONMENT_MODE.p.name()).equalsIgnoreCase(ENVIRONMENT_MODE.dev.name());
    }
}
