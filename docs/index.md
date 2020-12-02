# Gradle Doctor

![Github Actions](https://github.com/runningcode/gradle-doctor/workflows/CI/badge.svg)

The right prescription for your Gradle build.

Watch this Virtual Android Makers 2019 entitled [The Secrets of the Build Scan Plugin and the Internals of Gradle](https://www.youtube.com/watch?v=lgaqS0pmUzk) to learn more about what this plugin does.

## Usage
1. Apply the Gradle Plugin to the root of your project.
2. The Gradle Doctor will print suggestions for your build as you run regular tasks.

=== "Groovy"
    ``` groovy
    plugins {
      id "com.osacky.doctor" version "{{ doctor.current_release }}"
    }
    ```
=== "Kotlin"
    ``` kotlin
    plugins {
      id("com.osacky.doctor") version "{{ doctor.current_release }}"
    }
    ```
[For legacy plugin application, see the Gradle Plugin Portal.](https://plugins.gradle.org/plugin/com.osacky.doctor)

## Features
* Configurable warnings for build speed problems
* Measure time spent in Dagger annotation processors. Use [Delect](http://github.com/soundcloud/delect) to save time.
* Ensure `JAVA_HOME` is set and matches IDE's `JAVA_HOME`
* Easily disable test caching. Tests may not declare all inputs causing [false positives](https://github.com/gradle/gradle/issues/9151). [Needed until Gradle implements a sandbox.](https://github.com/gradle/gradle/issues/9210)
* Disable assembling all apps in repository simultaneously.
* Fail build when empty src directories are found. [Empty src directories](https://github.com/gradle/gradle/issues/2463) cause [cache misses](https://developers.soundcloud.com/blog/dagger-reflect).
* [Benchmarking remote build cache connection speed](remote-cache).
* [Warnings for negative avoidance saving tasks.](slower-from-cache)
* [Build scan tags to easily find and categorize builds with warnings](scan-tags).
* Warn when not using Parallel GC in Java 9+.

### Configurable Warnings
* Warn when build spends more than 10% of the time garbage collecting.
* Warn when connection to maven repositories is slowing down the build.
* Warn when build cache connection speed is slowing down the build.
