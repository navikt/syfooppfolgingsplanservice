package no.nav.syfo.config.ws;

import no.nav.syfo.scheduler.ScheduledTask;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.inject.Inject;
import java.util.List;

@Configuration
@Profile("remote")
public class SchedulingConfigurerImpl implements SchedulingConfigurer {

    private List<ScheduledTask> scheduledTasks;

    private TaskScheduler taskScheduler;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        scheduledTasks.forEach(r -> taskScheduler.schedule(r, r.getTrigger()));
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

    @Inject
    public void setScheduledTasks(List<ScheduledTask> scheduledTasks) {
        this.scheduledTasks = scheduledTasks;
    }

    @Inject
    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }
}
