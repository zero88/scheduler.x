package io.github.zero88.schedulerx;

import java.time.Duration;

import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.vertx.core.Vertx;
import io.vertx.docgen.Source;

@Source
public class SchedulerxBasic {

    public void getStarted(Vertx vertx) {
        IntervalTrigger trigger = IntervalTrigger.builder().interval(Duration.ofSeconds(5)).build();            // <1>
        Job<Void, Void> job = (jobData, executionContext) -> System.out.println("Get started with scheduler.x");// <2>
        IntervalScheduler scheduler = IntervalScheduler.<Void, Void>builder()                                   // <3>
                                                       .setVertx(vertx).setTrigger(trigger).setJob(job).build();
        // After starting, every 5 seconds, the program will print to
        // the console log with the text `Get started with scheduler.x`
        scheduler.start();                                                                                      // <4>
        // Do other stuff
        System.out.println("Are you ready to explore more?");
    }

    public void cancelInScheduler(Vertx vertx, Job<Void, Void> job) {
        CronTrigger trigger = CronTrigger.builder().expression("0 0/3 0 ? * * *").build();              // <1>
        CronScheduler scheduler = CronScheduler.<Void, Void>builder()                                   // <2>
                                               .setVertx(vertx).setTrigger(trigger).setJob(job).build();
        // Start scheduler
        scheduler.start();                                                                              // <3>
        // Do other stuff
        // ...
        // after a while later, you can cancel the `scheduler`.
        // And the `trigger` is no longer to fire in the future.
        scheduler.cancel();                                                                             // <4>
    }

}
