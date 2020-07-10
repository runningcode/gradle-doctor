package com.osacky.doctor

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

open class DoctorExtension(objects: ObjectFactory) {
    /**
     * Throw an exception when multiple Gradle Daemons are running.
     */
    val disallowMultipleDaemons = objects.property<Boolean>().convention(false)
    /**
     * Ensure that we are using JAVA_HOME to build with this Gradle.
     */
    val ensureJavaHomeMatches = objects.property<Boolean>().convention(true)
    /**
     * Ensure we have JAVA_HOME set.
     */
    val ensureJavaHomeIsSet = objects.property<Boolean>().convention(true)
    /**
     * Show a message if the download speed is less than this many megabytes / sec.
     */
    val downloadSpeedWarningThreshold = objects.property<Float>().convention(0.5f)
    /**
     * The level at which to warn when a build spends more than this percent garbage collecting.
     */
    val GCWarningThreshold = objects.property<Float>().convention(0.10f)
    /**
     * Print a warning to the console if we spend more than this amount of time with Dagger annoation processors.
     */
    val daggerThreshold = objects.property<Int>().convention(5000)
    /**
     * By default, Gradle caches test results. This can be dangerous if tests rely on timestamps, dates, or other files
     * which are not declared as inputs.
     */
    val enableTestCaching = objects.property<Boolean>().convention(true)
    /**
     * By default, Gradle treats empty directories as inputs to compilation tasks. This can cause cache misses.
     */
    val failOnEmptyDirectories = objects.property<Boolean>().convention(true)

    /**
     * Do not allow building all apps simultaneously. This is likely not what the user intended.
     */
    val allowBuildingAllAndroidAppsSimultaneously = objects.property<Boolean>().convention(false)
}
