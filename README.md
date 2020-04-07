# Gradle Doctor

[![CircleCI](https://circleci.com/gh/runningcode/gradle-doctor/tree/master.svg?style=svg)](https://circleci.com/gh/runningcode/gradle-doctor/tree/master)

The right prescription for your Gradle build.

## Usage

``` groovy
buildscript {
  dependencies {
    classpath "com.osacky.doctor:doctor-plugin:0.3.3"
  }
}

// Must be applied in the project root.
apply plugin: "com.osacky.doctor"
```

## Features
* Configurable warnings for build speed problems
* Measure time spent in Dagger annotation processors. Use [Delect](http://github.com/soundcloud/delect) to save time.
* Ensure `JAVA_HOME` is set and matches IDE's `JAVA_HOME`
* Easily disable test caching. Tests may not declare all inputs causing [false positives](https://github.com/gradle/gradle/issues/9151). [Needed until Gradle implements a sandbox.](https://github.com/gradle/gradle/issues/9210)
* Disable assembling all apps in repository simultaneously.
* Fail build when empty src directories are found. [Empty src directories](https://github.com/gradle/gradle/issues/2463) cause [cache misses](https://developers.soundcloud.com/blog/dagger-reflect).
* Benchmarking remote build cache connection speed.

### Configurable Warnings
* Warn when build spends more than 10% of the time garbage collecting.
* Warn when connection to maven repositories is slowing down the build.
* Warn when build cache connection speed is slowing down the build.

Here are the configuration options with their defaults:
``` groovy
doctor {
  /**
   * Throw an exception when multiple Gradle Daemons are running.
   */
  disallowMultipleDaemons = false
  /**
   * Ensure that we are using JAVA_HOME to build with this Gradle.
   */
  ensureJavaHomeMatches = true
  /**
   * Ensure we have JAVA_HOME set.
   */
  ensureJavaHomeIsSet = true
  /**
   * Show a message if the download speed is less than this many megabytes / sec.
   */
  downloadSpeedWarningThreshold = .5f
  /**
   * The level at which to warn when a build spends more than this percent garbage collecting.
   */
  GCWarningThreshold = 0.10f
  /**
   * Print a warning to the console if we spend more than this amount of time with Dagger annoation processors.
   */
  daggerThreshold = 5000
  /**
   * By default, Gradle caches test results. This can be dangerous if tests rely on timestamps, dates, or other files
   * which are not declared as inputs.
   */
  enableTestCaching = true
  /**
   * By default, Gradle treats empty directories as inputs to compilation tasks. This can cause cache misses.
   */
  failOnEmptyDirectories = true
  /**
   * Do not allow building all apps simultaneously. This is likely not what the user intended.
   */
  allowBuildingAllAndroidAppsSimultaneously = false
}
```
[Configuration extension code is here.](https://github.com/runningcode/gradle-doctor/blob/master/doctor-plugin/src/main/java/com/osacky/doctor/DoctorExtension.kt)

## Remote Build Cache Benchmark
To enable, run a gradle task that you would like to profile with the flag `-PbenchmarkRemoteCache`.

For example:
`./gradlew :app:assembleDebug -PbenchmarkRemoteCache`

The result will be output like so:
```
=============================== Gradle Doctor Prescriptions ============================================
| = Remote Build Cache Benchmark Report =                                                              |
| Executed tasks created compressed artifacts of size 7 MB                                             |
| Total Task execution time was 86,68 s                                                                |
|                                                                                                      |
| To save time, you need an estimated connection to the build cache node of at least 0,08 MB/s.        |
| Check a build scan to see your connection speed to the build cache node.                             |
| Build cache node throughput may be different than your internet connection speed.                    |
|                                                                                                      |
| A 1 MB/s connection would save you 79.68 s.                                                         |
| A 2 MB/s connection would save you 83.18 s.                                                         |
| A 10 MB/s connection would save you 85.98 s.                                                        |
|                                                                                                      |
| Note: This is an estimate. Real world performance may vary. This estimate does not take in to accoun |
| t time spent decompressing cached artifacts or roundtrip communication time to the cache node.       |
========================================================================================================
```

## Publishing
``` bash
./gradlew publishToMavenCentral -Dorg.gradle.internal.publish.checksums.insecure=true
./gradlew publishToGradlePlugin
```
