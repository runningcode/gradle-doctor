# Gradle Doctor

[![CircleCI](https://circleci.com/gh/runningcode/gradle-doctor/tree/master.svg?style=svg)](https://circleci.com/gh/runningcode/gradle-doctor/tree/master)

The right prescription for your Gradle build.


## Usage

More details coming soon.

``` groovy
buildscript {
  dependencies {
    classpath "com.osacky.doctor:doctor-plugin:0.1.6"
  }
}

// Must be applied in the project root.
apply plugin: "com.osacky.doctor"
```

## Publishing
``` bash
./gradlew publishToMavenCentral
```
