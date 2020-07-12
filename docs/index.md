# Gradle Doctor

[![CircleCI](https://circleci.com/gh/runningcode/gradle-doctor/tree/master.svg?style=svg)](https://circleci.com/gh/runningcode/gradle-doctor/tree/master)

The right prescription for your Gradle build.

Watch this Virtual Android Makers 2019 entitled [The Secrets of the Build Scan Plugin and the Internals of Gradle](https://www.youtube.com/watch?v=lgaqS0pmUzk) to learn more about what this plugin does.

## Usage

``` groovy
buildscript {
  dependencies {
    classpath "com.osacky.doctor:doctor-plugin:{{ doctor.current_release }}"
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

