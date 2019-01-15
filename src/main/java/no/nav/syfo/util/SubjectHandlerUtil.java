package no.nav.syfo.util;

import static no.nav.common.auth.SubjectHandler.getIdent;

public class SubjectHandlerUtil {

    public static String getUserId() {
        return getIdent().orElseThrow(IllegalArgumentException::new);
    }
}
