package no.nav.syfo.scheduler;

import org.slf4j.Logger;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

public interface ScheduledTask extends Runnable {

    //Default cron-verdi er satt til klokken 00.00 hvert døgn
    String MIDNATT = "0 0 0 * * *";

    Function<String, Trigger> GET_FASIT_TRIGGER = className -> new CronTrigger(
            Optional
                    .ofNullable(className)
                    .map(s -> format("SERVICEOPPFOELGINGSDIALOG_SCHEDULER_%1s_CRON", className))
                    .map(p -> getProperty(p, MIDNATT))
                    .orElse(MIDNATT)
    );

    default Trigger getTrigger() {
        return GET_FASIT_TRIGGER.apply(this.getClass().getSimpleName());
    }

    default void runWithCallId() {
        throw new RuntimeException("En ScheduledTask må override enten run() eller runWithCallId()");
    }

    default Logger getLog() {
        return getLogger(ScheduledTask.class);
    }
}
