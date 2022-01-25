# Remote Build Cache Benchmark

### More information
How fast does my internet connection need to be in order to benefit from the Gradle Remote Build Cache?

Read [Part 1](https://medium.com/swlh/how-fast-does-my-internet-need-to-be-to-use-the-gradle-remote-build-cache-part-1-4acaa6f9a2fa) and [Part 2](https://medium.com/@runningcode/how-fast-does-my-internet-need-to-be-to-use-the-gradle-remote-build-cache-part-2-1bc2b171f19)


## Basic Benchmark
To start the benchmark, run a Gradle task that you would like to profile with the flag
```
-PbenchmarkRemoteCache
```

To force tasks to rerun for the benchmark, use `-PrerunSourceTasksForBenchmark` and `-PrerunLargeOutputTasksForBenchmark` to control which tasks are forced to re-run as part of the benchmark.

Example full benchmark scenario:
``` bash
./gradlew :app:assembleDebug -PbenchmarkRemoteCache -PrerunSourceTasksForBenchmark -PrerunLargeOutputTasksForBenchmark
```


## Customized Benchmark Scenario
You can also omit the rerun properties in order to measure the individual performance of specific tasks by forcing tasks to rerun with `outputs.upToDateWhen { false }`. For example:
``` groovy
tasks.withType(SourceTask).configureEach {
  outputs.upToDateWhen { false }
}
```

Then run the specific benchmark scenario like so:
``` bash
./gradlew :app:assembleDebug -PbenchmarkRemoteCache
```


## Benchmark Result Report
This is an example remote cache benchmark report.
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

