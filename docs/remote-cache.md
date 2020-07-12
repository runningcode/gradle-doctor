## Remote Build Cache Benchmark

[How fast does my internet connection need to be in order to benefit from the Gradle Remote Build Cache Part 1](https://medium.com/swlh/how-fast-does-my-internet-need-to-be-to-use-the-gradle-remote-build-cache-part-1-4acaa6f9a2fa)

[How fast does my internet connection need to be in order to benefit from the Gradle Remote Build Cache Part 2](https://medium.com/@runningcode/how-fast-does-my-internet-need-to-be-to-use-the-gradle-remote-build-cache-part-2-1bc2b171f19)


To start the benchmark, run a Gradle task that you would like to profile with the flag `-PbenchmarkRemoteCache`.
To force tasks to rerun for the benchmark, use `-PrerunSourceTasksForBenchmark` and `-PrerunLargeOutputTasksForBenchmark` to control which tasks are forced to re-run as part of the benchmark.
You can also omit those flags and measure the individual performance of certains tasks by marking the tasks with `outputs.upToDateWhen { false }`.

For example:
`./gradlew :app:assembleDebug -PbenchmarkRemoteCache -PrerunSourceTasksForBenchmark -PrerunLargeOutputTasksForBenchmark`

The result will be output like so:
```
=============================== Gradle Doctor Prescriptions ============================================
| = Remote Build Cache Benchmark Report =                                                              |
| Executed tasks created compressed artifacts of size 159,93 MB                                        |
| Total Task execution time was 208,85 s                                                               |
|                                                                                                      |
| In order for a remote build cache to save you time, you would need an internet connection to your    |
| node of at least 0,77 MB/s.                                                                          |
| Check a build scan to see your connection speed to the build cache node.                             |
| Build cache node throughput may be different than your internet connection speed.                    |
|                                                                                                      |
| A 1 MB/s connection would save you 48,92 s.                                                          |
| A 2 MB/s connection would save you 128,88 s.                                                         |
| A 10 MB/s connection would save you 192,86 s. i                                                      |
|                                                                                                      |
| Note: This is an estimate. Real world performance may vary. This estimate does not take in to accoun |
| t time spent decompressing cached artifacts or roundtrip communication time to the cache node.       |
========================================================================================================
```

