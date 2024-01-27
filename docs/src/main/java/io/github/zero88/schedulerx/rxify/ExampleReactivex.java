package io.github.zero88.schedulerx.rxify;

import io.github.zero88.schedulerx.Job;
import io.github.zero88.schedulerx.trigger.CronTrigger;
import io.github.zero88.schedulerx.trigger.EventTrigger;
import io.github.zero88.schedulerx.trigger.IntervalTrigger;
import io.vertx.core.json.JsonObject;
import io.vertx.docgen.Source;

@Source
public class ExampleReactivex {

    public void rx3(io.vertx.core.Vertx vertx, IntervalTrigger trigger, Job<Object, Object> job) {
        io.github.zero88.schedulerx.IntervalScheduler scheduler
            = io.github.zero88.schedulerx.IntervalScheduler.builder()
                                                           .setVertx(vertx)
                                                           .setTrigger(trigger)
                                                           .setJob(job)
                                                           .build();
        // create rx3 schedulerx by non-rxify version
        io.github.zero88.schedulerx.rxjava3.IntervalScheduler rxScheduler
            = io.github.zero88.schedulerx.rxjava3.IntervalScheduler.newInstance(scheduler);
        rxScheduler.start();
    }

    public void rx3Builder(io.vertx.core.Vertx vertx, CronTrigger trigger, Job<Object, Object> job) {
        io.vertx.rxjava3.core.Vertx vertxRx3 = io.vertx.rxjava3.core.Vertx.newInstance(vertx);
        io.vertx.rxjava3.core.WorkerExecutor workerExecutor = vertxRx3.createSharedWorkerExecutor("hello-rx3");

        // Create rx3 schedulerx by builder
        io.github.zero88.schedulerx.rxjava3.CronScheduler rxScheduler
            = io.github.zero88.schedulerx.rxjava3.CronScheduler.builder()
                                                               .setVertx(vertxRx3)
                                                               .setTrigger(trigger)
                                                               .setJob(job)
                                                               .build();
        // Start scheduler with rx3 worker
        rxScheduler.start(workerExecutor);
    }

    public void mutinyBuilder(io.vertx.core.Vertx vertx, EventTrigger<JsonObject> trigger, Job<Object, Object> job) {
        io.vertx.mutiny.core.Vertx vertxMutiny = io.vertx.mutiny.core.Vertx.newInstance(vertx);
        io.vertx.mutiny.core.WorkerExecutor workerExecutor = vertxMutiny.createSharedWorkerExecutor("hello-mutiny");

        // Create mutiny scheduler by builder
        io.github.zero88.schedulerx.mutiny.EventScheduler<JsonObject> mutinyScheduler
            = io.github.zero88.schedulerx.mutiny.EventScheduler.<Object, Object, JsonObject>builder()
                                                               .setVertx(vertxMutiny)
                                                               .setTrigger(trigger)
                                                               .setJob(job)
                                                               .build();
        // Start scheduler with mutiny worker
        mutinyScheduler.start(workerExecutor);
    }

    public void mutinyBuilderWithMutinyJob(io.vertx.core.Vertx vertx, IntervalTrigger trigger) {
        io.vertx.mutiny.core.Vertx vertxMutiny = io.vertx.mutiny.core.Vertx.newInstance(vertx);
        // Create new example mutiny job instance
        ExampleMutinyJob job = new ExampleMutinyJob();
        // Create mutiny scheduler within Mutiny job then start
        io.github.zero88.schedulerx.mutiny.IntervalScheduler.<Void, String>builder()
                                                            .setVertx(vertxMutiny)
                                                            .setTrigger(trigger)
                                                            .setJob(job)
                                                            .build()
                                                            .start();
    }

}
