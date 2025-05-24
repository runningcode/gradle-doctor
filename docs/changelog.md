# Changelog

## 0.11.0
* [Drop support for Gradle 6.8 and earlier.](https://github.com/runningcode/gradle-doctor/pull/413)

## 0.10.0
* [Fix Develocity deprecation warnings](https://github.com/runningcode/gradle-doctor/pull/337)
* [Add support for checking executables for Java home](https://github.com/runningcode/gradle-doctor/pull/336) Thanks [Slow Pacer](https://github.com/slowpacer)

## 0.9.2
* [Introduce execution modes for AppleRosettaTranslationCheck](https://github.com/runningcode/gradle-doctor/pull/311) Thanks [Egor Andreevich](https://github.com/Egorand)

## 0.9.1
* [Ignore exit process status for rosetta translation check](https://github.com/runningcode/gradle-doctor/pull/285) Thanks [Andres Di Menna](https://github.com/ninniuz)

## 0.9.0
* [Implement Apple Rosetta Translation Check](https://github.com/runningcode/gradle-doctor/pull/220) Thanks [Gediminas Zukas](https://github.com/GediminasZukas)
* Bump Kotlin language version to 1.5

## 0.8.1
* [Fix compatibility with KGP 1.7.0](https://github.com/runningcode/gradle-doctor/issues/208) Thanks [ZacSweers](https://github.com/ZacSweers)

## 0.8.0
* [Skip multiple daemons check on non-Unix machines, not supported yet](https://github.com/runningcode/gradle-doctor/issues/84)
* [Detect Kotlin Compiler Daemon failing to connect.](https://github.com/runningcode/gradle-doctor/issues/194)
* [Support fail threshold for high GC usage](https://github.com/runningcode/gradle-doctor/issues/183)
* [Clean check is disabled in Gradle 7.4+, fixes compatibility with project isolation](https://github.com/runningcode/gradle-doctor/issues/180)

## 0.7.3
* Fix [compatiblity with Java 8.](https://github.com/runningcode/gradle-doctor/issues/171)

## 0.7.2
* Fix [exception when using Java 17 EA.](https://github.com/runningcode/gradle-doctor/issues/168) [PR](https://github.com/runningcode/gradle-doctor/pull/169) Thanks [ZacSweers](https://github.com/ZacSweers)
## 0.7.1
* Add error message for when the clean task has dependencies. [PR](https://github.com/runningcode/gradle-doctor/pull/149)
* Fix typo in `warnWhenJetifierEnabled` error message. [PR](https://github.com/runningcode/gradle-doctor/pull/158) Thanks [kelvinharron](https://github.com/kelvinharron)

## 0.7.0
* Don't check for empty source directories in Gradle 6.8. Gradle now ignores them by default. [PR](https://github.com/runningcode/gradle-doctor/pull/136)
* Warn when not using parallel GC in java 9+. [Fixes #125](https://github.com/runningcode/gradle-doctor/issues/125) [PR](xxx)
* Remove redundant "doctor" build scan tag.

## 0.6.3
* Fix ClassCastException caused by race condition. [PR](https://github.com/runningcode/gradle-doctor/pull/129)

## 0.6.2
* [Add threshold for negative avoidance savings.](https://github.com/runningcode/gradle-doctor/pull/126)

## 0.6.1
* [Add build scan values for negative avoidance savings tasks.](https://github.com/runningcode/gradle-doctor/pull/121)
* Prefix build scan tags with `doctor-` for easier searchability use wildcard `doctor-*` to find all build scans with doctor prescriptions.

## 0.6.0
* Add warning [when Android Jetifier is enabled.](https://github.com/runningcode/gradle-doctor/pull/118)
* Print tasks with [negative avoidance savings from the cache.](https://github.com/runningcode/gradle-doctor/pull/117) [Fixes #86](https://github.com/runningcode/gradle-doctor/issues/86)
* Add build scan tags to [easily search and categorize builds.](../scan-tags) [PR](https://github.com/runningcode/gradle-doctor/pull/119)

## 0.5.1
* Fix bugs in JAVA_HOME check [#110](https://github.com/runningcode/gradle-doctor/pull/110). Thanks [ZacSweers](https://github.com/ZacSweers)

## 0.5.0
* Check for farthest empty parent for reporting file to remove [#105](https://github.com/runningcode/gradle-doctor/pull/105). [Fixes #96](https://github.com/runningcode/gradle-doctor/issues/96) Thanks [ZacSweers](https://github.com/ZacSweers)

!!! Warning "Breaking API change"
    Add more granularity to `JAVA_HOME` checks [#104](https://github.com/runningcode/gradle-doctor/pull/104). [Fixes #98](https://github.com/runningcode/gradle-doctor/issues/98) Thanks [ZacSweers](https://github.com/ZacSweers)


## 0.4.3
* Experimental Configuration Cache support. Note: it is not fully supported, but it will not generate warnings.

## 0.4.2
* Fix bug when running remote build cache benchmark and specifying a custom build cache directory.

## 0.4.1
* Fix confusing warning about slow connection to maven repositories.
* Publish marker to maven central.

## 0.4.0
* More control over which tasks are re-run in remote cache benchmark. See README.md for more information.
* Use Gradle Properties for lazy configuration and future configuration caching support. (Breaking API change)
* Fix performance by not using `afterEvaluate`.

## 0.3.4
* Correctly calculate download speeds for build cache and remote repositories.

## 0.3.3
* Print out number of tasks which were forced to re-execute.

## 0.3.2
* Fix NPE when calculating remote benchmark cache.

## 0.3.1
* Fix megabyte estimation
* Also add include Android resource compilation tasks in remote build cache estimation.

## 0.3.0
* Ability to benchmark remote build cache connection speed.
