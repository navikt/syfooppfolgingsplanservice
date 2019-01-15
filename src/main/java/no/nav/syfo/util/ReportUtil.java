package no.nav.syfo.util;

import no.nav.metrics.Event;

import static no.nav.metrics.MetricsFactory.createEvent;

public class ReportUtil {
    public static void report(String hendelseNavn, String feltnavn, Object verdi) {
        Event antallDagerFraOpprettetTilBekreftet = createEvent(hendelseNavn);
        antallDagerFraOpprettetTilBekreftet.addFieldToReport(feltnavn, verdi);
        antallDagerFraOpprettetTilBekreftet.report();
    }
}
