# Gradle Doctor

[![CircleCI](https://circleci.com/gh/runningcode/gradle-doctor/tree/master.svg?style=svg)](https://circleci.com/gh/runningcode/gradle-doctor/tree/master)

The right prescription for your Gradle build.


## Usage

More details coming soon.

Snapshots are available in the sonatype snapshots repository.
``` groovy
buildscript {
  repositories {
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
  }
  dependencies {
    classpath "com.osacky.doctor:doctor-plugin:0.1.3-SNAPSHOT"
  }
}

// Must be applied in the project root.
apply plugin: "com.osacky.doctor"
```

## Publishing
``` bash
./gradlew publishToMavenCentral
```
