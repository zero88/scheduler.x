# Vertx Scheduler

A `lightweight plugable scheduler` based on plain [Vert.x](https://vertx.io/) core without any external libs for scheduling with _cron-style_ and _interval_ timers with a detail _monitor_ on both sync and async task.

- `Scheduling` with:
    - `cron-style` based on [Quartz](http://www.quartz-scheduler.org/) - `Unix` cron expression and `timezone` available
      on `JVM`
    - `interval` with given `time interval`, given `initial delay time` and `repeating` a given number of times or
      repeating infinitely
    - able to `cancel` schedule event
- Support `synchronize` task and `async` task
- Run on `vertx-worker-thread` or dedicated `vertx thread-pool`
- Monitor `executor event` includes `fire counting`, `fired round`, `event time`, `task result data`, `task error` on
  your need such as `on schedule`, `on misfire`, `on each round`, `on completed`, etc...
- Easy customize your `task` such as `HTTP client job`, `EventBus job`, `MQTT client job`, `database job`, etc...

## Usage

### Maven Dependency

```xml

<dependency>
    <groupId>io.github.zero88</groupId>
    <artifactId>vertx-scheduler</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle Dependency

```groovy
api("io.github.zero88:vertx-scheduler:1.0.0")
```

## How To

### Creating an executor

```java
IntervalTaskExecutor.builder()
    .vertx(vertx)
    .trigger(IntervalTrigger.builder().interval(1).repeat(10).build())
    .task((jobData,ctx)->{
    if(ctx.round()==5){
    ctx.forceStopExecution();
    }
    })
    .build()
    .start()
```

```java
CronTaskExecutor.builder()
    .vertx(vertx)
    .trigger(CronTrigger.builder().expression("0/5 * * ? * * *").build())
    .task((jobData,ctx)->{})
    .build()
    .start(vertx.createSharedWorkerExecutor("TEST_CRON",3));
```

### Monitor

Please
check [TaskExecutorMonitor](https://github.com/zero88/vertx-scheduler/blob/62d8feb265f45afad2626886c24f2899346f46b1/src/main/java/io/github/zero88/vertx/scheduler/TaskExecutorMonitor.java)
. Example:

```java
public interface TaskExecutorLogMonitor extends TaskExecutorMonitor {

    Logger LOGGER = LoggerFactory.getLogger(TaskExecutorLogMonitor.class);
    TaskExecutorMonitor LOG_MONITOR = new TaskExecutorLogMonitor() {};

    @Override
    default void onUnableSchedule(@NonNull TaskResult result) {
        LOGGER.error("Unable schedule task at [{}] due to error", result.unscheduledAt(), result.error());
    }

    @Override
    default void onSchedule(@NonNull TaskResult result) {
        if (result.isReschedule()) {
            LOGGER.debug("TaskExecutor is rescheduled at [{}] round [{}]", result.rescheduledAt(), result.round());
        } else {
            LOGGER.debug("TaskExecutor is available at [{}]", result.availableAt());
        }
    }

    @Override
    default void onMisfire(@NonNull TaskResult result) {
        LOGGER.debug("Misfire tick [{}] at [{}]", result.tick(), result.triggeredAt());
    }

    @Override
    default void onEach(@NonNull TaskResult result) {
        LOGGER.debug("Finish round [{}] - Is Error [{}] | Executed at [{}] - Finished at [{}]", result.round(),
                     result.isError(), result.executedAt(), result.finishedAt());
    }

    @Override
    default void onCompleted(@NonNull TaskResult result) {
        LOGGER.debug("Completed task in round [{}] at [{}]", result.round(), result.completedAt());
    }

}
```

### Custom task

Please
check [HttpClientTask](https://github.com/zero88/vertx-scheduler/blob/62d8feb265f45afad2626886c24f2899346f46b1/src/test/java/io/github/zero88/vertx/scheduler/custom/HttpClientTask.java)
and
its [test](https://github.com/zero88/vertx-scheduler/blob/62d8feb265f45afad2626886c24f2899346f46b1/src/test/java/io/github/zero88/vertx/scheduler/custom/HttpClientTaskTest.java)
for `async` task

```java
public class HttpClientTask implements Task {

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void execute(@NonNull JobData jobData, @NonNull TaskExecutionContext executionContext) {
        final Vertx vertx = executionContext.vertx();
        JsonObject url = (JsonObject) jobData.get();
        vertx.createHttpClient().request(HttpMethod.GET, url.getString("host"), url.getString("path"), ar1 -> {
            if (ar1.succeeded()) {
                HttpClientRequest request = ar1.result();
                request.send(ar2 -> {
                    if (ar2.succeeded()) {
                        HttpClientResponse response = ar2.result();
                        response.body(ar3 -> {
                            if (ar3.succeeded()) {
                                executionContext.complete(new JsonObject().put("status", response.statusCode())
                                                                          .put("response", ar3.result().toJson()));
                            } else {
                                executionContext.fail(ar3.cause());
                            }
                        });
                    } else {
                        executionContext.fail(ar2.cause());
                    }
                });
            } else {
                executionContext.fail(ar1.cause());
            }
        });
    }

}
```

## TODO

- [ ] Cron task with repeating until a given time / date
- [ ] Async query job data

## Disclaim and Disclosure

This is lightweight module then I will not support for adding any rich function like `manage group of task/trigger`
, `publish monitor event` by integrate with a fantastic client such as `Vertx EventBus`, etc... 

I'm planning to do it in `vertx-planner` project later. Stay a tune!!!
