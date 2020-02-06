# Gradle Doctor

[![CircleCI](https://circleci.com/gh/runningcode/gradle-doctor/tree/master.svg?style=svg)](https://circleci.com/gh/runningcode/gradle-doctor/tree/master)

The right prescription for your Gradle build.

## Usage

``` groovy
buildscript {
  dependencies {
    classpath "com.osacky.doctor:doctor-plugin:0.1.12"
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

## Publishing
``` bash
./gradlew publishToMavenCentral
```
