# Changelog

## [v2.0.0-rc.1](https://github.com/zero88/scheduler.x/tree/v2.0.0-rc.1) (2024-01-18)

[Full Changelog](https://github.com/zero88/scheduler.x/compare/925dfb386184b7a6eb1b22e59d6e3125d85f5e1b...v2.0.0-rc.1)

**Breaking changes:**

- Rename `Task` to `Job` [\#92](https://github.com/zero88/scheduler.x/issues/92)

**Implemented enhancements:**

- Add AsyncJob [\#105](https://github.com/zero88/scheduler.x/issues/105)
- Improve SchedulingMonitor  [\#103](https://github.com/zero88/scheduler.x/issues/103)
- Wrap `Instant.now()` to interface and make it injectable into `Scheduler` [\#100](https://github.com/zero88/scheduler.x/issues/100)
- Improve Trigger [\#97](https://github.com/zero88/scheduler.x/issues/97)
- Add TriggerEvaluator [\#93](https://github.com/zero88/scheduler.x/issues/93)
- Add TriggerContext condition state `executed` [\#91](https://github.com/zero88/scheduler.x/issues/91)
- Add Timeout configuration in execution [\#88](https://github.com/zero88/scheduler.x/issues/88)
- Add leeway time for trigger rule [\#82](https://github.com/zero88/scheduler.x/issues/82)
- Change `the evaluation on the trigger before run` to async operation [\#77](https://github.com/zero88/scheduler.x/issues/77)
- Add TriggerCondition [\#69](https://github.com/zero88/scheduler.x/issues/69)
- EventTrigger should be able to persist [\#67](https://github.com/zero88/scheduler.x/issues/67)
- Class naming convention [\#66](https://github.com/zero88/scheduler.x/issues/66)
- Trigger should be able to control its executor to start or stop [\#64](https://github.com/zero88/scheduler.x/issues/64)
- Trigger representation [\#63](https://github.com/zero88/scheduler.x/issues/63)
- Add event trigger [\#60](https://github.com/zero88/scheduler.x/issues/60)
- Add advance trigger rule [\#57](https://github.com/zero88/scheduler.x/issues/57)
- Add external id in JobData [\#2](https://github.com/zero88/scheduler.x/issues/2)
- Cron task with repeating until a given time / date [\#1](https://github.com/zero88/scheduler.x/issues/1)

**Fixed bugs:**

- Cron Trigger still run one more round after invoking force stop in current round [\#68](https://github.com/zero88/scheduler.x/issues/68)
- Prevent to start the executor many times [\#62](https://github.com/zero88/scheduler.x/issues/62)

**Closed issues:**

- Change Timeframe\#from to minimum allowed value \(inclusive\) [\#84](https://github.com/zero88/scheduler.x/issues/84)
- Preview schedule  [\#56](https://github.com/zero88/scheduler.x/issues/56)
- Make contract between Task and TaskExecutionState + TaskResult [\#53](https://github.com/zero88/scheduler.x/issues/53)
- Make contract between Task and  JobData [\#52](https://github.com/zero88/scheduler.x/issues/52)
- Upgrade vertx [\#48](https://github.com/zero88/scheduler.x/issues/48)
- Rename artifact [\#47](https://github.com/zero88/scheduler.x/issues/47)
- Upgrade gradle [\#46](https://github.com/zero88/scheduler.x/issues/46)
- Remove lombok [\#45](https://github.com/zero88/scheduler.x/issues/45)

**Merged pull requests:**

- feat\(\#105\): Add reactive Job interface [\#108](https://github.com/zero88/scheduler.x/pull/108) ([zero88](https://github.com/zero88))
- Doc/prepare release 2.0.0 [\#107](https://github.com/zero88/scheduler.x/pull/107) ([zero88](https://github.com/zero88))
- feat\(\#105\): Add AsyncJob and reactive version \(rx3/mutiny\) [\#106](https://github.com/zero88/scheduler.x/pull/106) ([zero88](https://github.com/zero88))
- Feature/improve scheduling monitor [\#104](https://github.com/zero88/scheduler.x/pull/104) ([zero88](https://github.com/zero88))
- refactor\(Scheduler\): Simplify Scheduler interface [\#102](https://github.com/zero88/scheduler.x/pull/102) ([zero88](https://github.com/zero88))
- feature\(\#100\): TimeClock interface [\#101](https://github.com/zero88/scheduler.x/pull/101) ([zero88](https://github.com/zero88))
- fix\(\#93\): the evaluation chaining operation must stop when reach a trigger target status [\#99](https://github.com/zero88/scheduler.x/pull/99) ([zero88](https://github.com/zero88))
- Feature/improve interval trigger [\#98](https://github.com/zero88/scheduler.x/pull/98) ([zero88](https://github.com/zero88))
- Feature/refactor internal api [\#96](https://github.com/zero88/scheduler.x/pull/96) ([zero88](https://github.com/zero88))
- Feature/trigger evaluation [\#95](https://github.com/zero88/scheduler.x/pull/95) ([zero88](https://github.com/zero88))
- Feature/rename task to job [\#94](https://github.com/zero88/scheduler.x/pull/94) ([zero88](https://github.com/zero88))
- Feature/add trigger state executed [\#90](https://github.com/zero88/scheduler.x/pull/90) ([zero88](https://github.com/zero88))
- Feature/add timeout [\#89](https://github.com/zero88/scheduler.x/pull/89) ([zero88](https://github.com/zero88))
- chore\(gradle\): update build dependencies [\#87](https://github.com/zero88/scheduler.x/pull/87) ([zero88](https://github.com/zero88))
- feat\(Timeframe\): change `from` to an inclusive value \[\#84\] [\#85](https://github.com/zero88/scheduler.x/pull/85) ([zero88](https://github.com/zero88))
- feat\(TriggerRule\): Add leeway configuration [\#83](https://github.com/zero88/scheduler.x/pull/83) ([zero88](https://github.com/zero88))
- Feature/evaluate trigger in blocking thread [\#80](https://github.com/zero88/scheduler.x/pull/80) ([zero88](https://github.com/zero88))
- Bugfix/fix cron trigger still run one more round [\#79](https://github.com/zero88/scheduler.x/pull/79) ([zero88](https://github.com/zero88))
- Implement serialization/deserialization [\#76](https://github.com/zero88/scheduler.x/pull/76) ([zero88](https://github.com/zero88))
- Refactor/misc [\#75](https://github.com/zero88/scheduler.x/pull/75) ([zero88](https://github.com/zero88))
- feat\(\#57\): Add Trigger rule [\#74](https://github.com/zero88/scheduler.x/pull/74) ([zero88](https://github.com/zero88))
- feat\(\#69\): Add TriggerCondition [\#73](https://github.com/zero88/scheduler.x/pull/73) ([zero88](https://github.com/zero88))
- feat\(\#63\): Trigger representation [\#72](https://github.com/zero88/scheduler.x/pull/72) ([zero88](https://github.com/zero88))
- Naming convention [\#70](https://github.com/zero88/scheduler.x/pull/70) ([zero88](https://github.com/zero88))
- Feature/some improvements [\#65](https://github.com/zero88/scheduler.x/pull/65) ([zero88](https://github.com/zero88))
- Feature/add event trigger [\#61](https://github.com/zero88/scheduler.x/pull/61) ([zero88](https://github.com/zero88))
- Add Trigger\#preview [\#58](https://github.com/zero88/scheduler.x/pull/58) ([zero88](https://github.com/zero88))
- Add generic parameter to make contract between Task and Input/Output [\#54](https://github.com/zero88/scheduler.x/pull/54) ([zero88](https://github.com/zero88))
- Feature/upgrade vertx [\#51](https://github.com/zero88/scheduler.x/pull/51) ([zero88](https://github.com/zero88))
- Feature/remove lombok [\#50](https://github.com/zero88/scheduler.x/pull/50) ([zero88](https://github.com/zero88))
- Feature/upgrade [\#49](https://github.com/zero88/scheduler.x/pull/49) ([zero88](https://github.com/zero88))



*This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*

