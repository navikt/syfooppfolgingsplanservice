package no.nav.syfo.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

public class DatoUtil {

    public static int antallDagerIPeriode(LocalDate fom, LocalDate tom) {
        return (int) (DAYS.between(fom, tom) + 1);
    }

    public static int dagerMellom(LocalDate tidspunkt1, LocalDate tidspunkt2) {
        Set helgedager = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        int dagerMellom = 0;
        while (tidspunkt1.isBefore(tidspunkt2)) {
            if (!helgedager.contains(tidspunkt1.getDayOfWeek())) {
                dagerMellom++;
            }
            tidspunkt1 = tidspunkt1.plusDays(1);
        }
        return dagerMellom;
    }
}
