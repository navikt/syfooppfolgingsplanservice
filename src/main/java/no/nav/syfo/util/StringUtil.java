package no.nav.syfo.util;

import org.apache.commons.lang.StringUtils;

public class StringUtil {
    public static String lowerCapitalize(String input) {
        return StringUtils.capitalize(input.toLowerCase());
    }
}
