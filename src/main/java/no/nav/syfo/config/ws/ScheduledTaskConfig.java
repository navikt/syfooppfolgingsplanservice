package no.nav.syfo.config.ws;

import no.nav.syfo.scheduler.AsynkOppgaverRapportScheduledTask;
import no.nav.syfo.scheduler.AsynkOppgaverScheduledTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

@Configuration
public class ScheduledTaskConfig {

    @Bean(name = "AsynkOppgaverScheduledTask")
    public AsynkOppgaverScheduledTask asynkOppgaverScheduledTask() {
        return new AsynkOppgaverScheduledTask();
    }

    @Bean(name = "AsynkOppgaverRapportScheduledTask")
    public AsynkOppgaverRapportScheduledTask asynkOppgaverRapportScheduledTask() {
        return new AsynkOppgaverRapportScheduledTask();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }
}
