# Build Scan Tags

Gradle Doctor automatically [adds build scan tags](https://docs.gradle.com/develocity/gradle-plugin/current/#extending_build_scans) to help categorize and filter builds when using [Develocity](https://gradle.com/develocity/).

The `doctor` tag will be added to a build when a prescription is suggested by the Gradle Doctor.

In addition to the `doctor` tag, the following tags will also be added depending on the prescription.
You can search for all of these tags by entering `doctor-*` in the tags in the build scan list.

* `doctor-high-gc` - build spends a longer percentage GCing than defined by `GCWarningThreshold`. Default 10%.
* `doctor-negative-savings` - build had a task that took longer to pull from the cache than it would take to re-execute. See [negative savings](../slower-from-cache) for information on how to avoid this.
* `doctor-slow-build-cache-connection` - build's connection speed to the build cache slower than the minimum speeds defined by `downloadSpeedWarningThreshold`. Default 0.5MB/s.
* `doctor-slow-maven-connection` - build's connection speed to maven repositories was slower than the minimum speeds defined by `downloadSpeedWarningThreshold`. Default 0.5MB/s.
* `doctor-long-dagger-time` - build spent longer with Dagger annotation processors than minimum defined by `daggerThreshold`. Default: 5 seconds.
* `doctor-java-home` - `JAVA_HOME` is not defined or does not match the `JAVA_HOME` used in this build.
