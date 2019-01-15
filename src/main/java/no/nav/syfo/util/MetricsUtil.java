package no.nav.syfo.util;

import no.nav.metrics.Event;

import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static no.nav.metrics.MetricsFactory.createEvent;
import static no.nav.syfo.util.DatoUtil.dagerMellom;

public class MetricsUtil {

    public static void reportAntallDagerSiden(LocalDateTime tidspunkt, String hendelseNavn) {
        Event antallDagerFraOpprettetTilBekreftet = createEvent(hendelseNavn);
        antallDagerFraOpprettetTilBekreftet.addFieldToReport("dager", dagerMellom(tidspunkt.toLocalDate(), now()));
        antallDagerFraOpprettetTilBekreftet.report();
    }
}
