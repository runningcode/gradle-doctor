package com.osacky.doctor

open class DoctorExtension {
    /**
     * Throw an exception when multiple Gradle Daemons are running.
     */
    var disallowMultipleDaemons = false
    /**
     * Ensure that we are using JAVA_HOME to build with this Gradle.
     */
    var ensureJavaHomeMatches = true
    /**
     * Ensure we have JAVA_HOME set.
     */
    var ensureJavaHomeIsSet = true
    /**
     * Show a message if the download speed is less than this many megabytes / sec.
     */
    var downloadSpeedWarningThreshold = .5f
    /**
     * The level at which to warn when a build spends more than this percent garbage collecting.
     */
    var GCWarningThreshold = 0.10f
    /**
     * Print a warning to the console if we spend more than this amount of time with Dagger annoation processors.
     */
    var daggerThreshold = 5000
    /**
     * By default, Gradle caches test results. This can be dangerous if tests rely on timestamps, dates, or other files
     * which are not declared as inputs.
     */
    var enableTestCaching = true
    /**
     * By default, Gradle treats empty directories as inputs to compilation tasks. This can cause cache misses.
     */
    var failOnEmptyDirectories = true

    /**
     * Do not allow building all apps simultaneously. This is likely not what the user intended.
     */
    var allowBuildingAllAndroidAppsSimultaneously = false
}
