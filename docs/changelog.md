# Changelog

## Unreleased

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
