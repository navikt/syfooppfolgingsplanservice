package no.nav.syfo.util.time;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public DateUtil() { }

    public static String tilMuntligDatoAarFormat(LocalDate dato) {
        return dato.getDayOfMonth() + ". " + mnd(dato.getMonth()) + " " + dato.getYear();
    }

    private static String mnd(Month month) {
        switch (month) {
            case JANUARY:
                return "januar";
            case FEBRUARY:
                return "februar";
            case MARCH:
                return "mars";
            case APRIL:
                return "april";
            case MAY:
                return "mai";
            case JUNE:
                return "juni";
            case JULY:
                return "juli";
            case AUGUST:
                return "august";
            case SEPTEMBER:
                return "september";
            case OCTOBER:
                return "oktober";
            case NOVEMBER:
                return "november";
            case DECEMBER:
                return "desember";
            default:
                return "";
        }
    }
}
