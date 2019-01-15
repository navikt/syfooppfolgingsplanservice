package no.nav.syfo.scheduler;

import org.junit.Test;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ScheduledTaskTest {

    private final String OPPFOLGINGSPLANSCHEDULER_CRON = "SERVICEOPPFOELGINGSDIALOG_SCHEDULER_ _CRON";

    @Test
    public void getTriggerDefault() throws Exception {
        clearProperty(OPPFOLGINGSPLANSCHEDULER_CRON);

        TriggerContext triggerContext = mock(TriggerContext.class);

        LocalDateTime now = now();
        when(triggerContext.lastCompletionTime()).thenReturn(convertToDate(now));

        Date nextExecutionTime = new ScheduledTask() {
            @Override
            public void run() {

            }
        }.getTrigger().nextExecutionTime(triggerContext);

        assertThat(nextExecutionTime).isEqualTo(convertToDate(now
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusDays(1)));
    }

    @Test
    public void getTriggerHvertSekund() throws Exception {
        setProperty(OPPFOLGINGSPLANSCHEDULER_CRON, "* * * * * *");

        TriggerContext triggerContext = mock(TriggerContext.class);

        LocalDateTime now = now();
        when(triggerContext.lastCompletionTime()).thenReturn(convertToDate(now));

        Date nextExecutionTime = new ScheduledTask() {
            @Override
            public void run() {

            }
        }.getTrigger().nextExecutionTime(triggerContext);

        assertThat(nextExecutionTime).isEqualTo(convertToDate(now
                .withNano(0)
                .plusSeconds(1)));
    }

    @Test
    public void getTriggerHvertMinutt() throws Exception {
        setProperty(OPPFOLGINGSPLANSCHEDULER_CRON, "0 * * * * *");

        TriggerContext triggerContext = mock(TriggerContext.class);

        LocalDateTime now = now();
        when(triggerContext.lastCompletionTime()).thenReturn(convertToDate(now));

        Date nextExecutionTime = new ScheduledTask() {
            @Override
            public void run() {

            }
        }.getTrigger().nextExecutionTime(triggerContext);

        assertThat(nextExecutionTime).isEqualTo(convertToDate(now
                .withSecond(0)
                .withNano(0)
                .plusMinutes(1)));
    }

    @Test
    public void getTriggerHverTime() throws Exception {
        setProperty(OPPFOLGINGSPLANSCHEDULER_CRON, "0 0 * * * *");

        TriggerContext triggerContext = mock(TriggerContext.class);

        LocalDateTime now = now();
        when(triggerContext.lastCompletionTime()).thenReturn(convertToDate(now));

        Date nextExecutionTime = new ScheduledTask() {
            @Override
            public void run() {

            }
        }.getTrigger().nextExecutionTime(triggerContext);

        assertThat(nextExecutionTime).isEqualTo(convertToDate(now
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusHours(1)));
    }

    @Test
    public void getTriggerHvertDogn() throws Exception {
        setProperty(OPPFOLGINGSPLANSCHEDULER_CRON, "0 0 0 * * *");

        TriggerContext triggerContext = mock(TriggerContext.class);

        LocalDateTime now = now();
        when(triggerContext.lastCompletionTime()).thenReturn(convertToDate(now));

        Date nextExecutionTime = new ScheduledTask() {
            @Override
            public void run() {

            }
        }.getTrigger().nextExecutionTime(triggerContext);

        assertThat(nextExecutionTime).isEqualTo(convertToDate(now
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusDays(1)));
    }

    @Test
    public void getTriggerHverManed() throws Exception {
        setProperty(OPPFOLGINGSPLANSCHEDULER_CRON, "0 0 0 1 * *");

        TriggerContext triggerContext = mock(TriggerContext.class);

        LocalDateTime now = now();
        when(triggerContext.lastCompletionTime()).thenReturn(convertToDate(now));

        Date nextExecutionTime = new ScheduledTask() {
            @Override
            public void run() {

            }
        }.getTrigger().nextExecutionTime(triggerContext);

        assertThat(nextExecutionTime).isEqualTo(convertToDate(now
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusMonths(1)));
    }

    @Test
    public void getTriggerHvertAr() throws Exception {
        setProperty(OPPFOLGINGSPLANSCHEDULER_CRON, "0 0 0 1 1 *");

        TriggerContext triggerContext = mock(TriggerContext.class);

        LocalDateTime now = now();
        when(triggerContext.lastCompletionTime()).thenReturn(convertToDate(now));

        Date nextExecutionTime = new ScheduledTask() {
            @Override
            public void run() {

            }
        }.getTrigger().nextExecutionTime(triggerContext);

        assertThat(nextExecutionTime).isEqualTo(convertToDate(now
                .withMonth(1)
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusYears(1)));
    }

    @Test
    public void getTriggerHverUke() throws Exception {
        setProperty(OPPFOLGINGSPLANSCHEDULER_CRON, "0 0 0 * * 1");

        TriggerContext triggerContext = mock(TriggerContext.class);

        LocalDateTime now = now();
        when(triggerContext.lastCompletionTime()).thenReturn(convertToDate(now));

        final Trigger trigger = new ScheduledTask() {
            @Override
            public void run() {

            }
        }.getTrigger();
        Date nextExecutionTime = trigger.nextExecutionTime(triggerContext);

        assertThat(nextExecutionTime).isEqualTo(convertToDate(now
                .with(DAY_OF_WEEK, 1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusWeeks(1)));
    }

    private Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
