package com.osacky.doctor

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class DoctorExtension(objects: ObjectFactory) {

    internal val javaHomeHandler = objects.newInstance<JavaHomeHandler>()

    /**
     * Throw an exception when multiple Gradle Daemons are running.
     */
    val disallowMultipleDaemons = objects.property<Boolean>().convention(false)
    /**
     * Show a message if the download speed is less than this many megabytes / sec.
     */
    val downloadSpeedWarningThreshold = objects.property<Float>().convention(0.5f)
    /**
     * The level at which to warn when a build spends more than this percent garbage collecting.
     */
    val GCWarningThreshold = objects.property<Float>().convention(0.10f)
    /**
     * Print a warning to the console if we spend more than this amount of time with Dagger annotation processors.
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

    /**
     * Warn if using Android Jetifier
     */
    val warnWhenJetifierEnabled = objects.property<Boolean>().convention(true)

    /**
     * Negative Avoidance Savings Threshold
     * By default the Gradle Doctor will print out a warning when a task is slower to pull from the cache than to
     * re-execute. There is some variance in the amount of time a task can take when several tasks are running
     * concurrently. In order to account for this there is a threshold you can set. When the difference is above the
     * threshold, a warning is displayed.
     */
    val negativeAvoidanceThreshold = objects.property<Int>().convention(500)

    /**
     * Configures `JAVA_HOME`-specific behavior.
     */
    fun javaHome(action: Action<JavaHomeHandler>) {
        action.execute(javaHomeHandler)
    }
}

abstract class JavaHomeHandler @Inject constructor(objects: ObjectFactory) {
    /**
     * Ensure that we are using `JAVA_HOME` to build with this Gradle.
     */
    val ensureJavaHomeMatches = objects.property<Boolean>().convention(true)
    /**
     * Ensure we have `JAVA_HOME` set.
     */
    val ensureJavaHomeIsSet = objects.property<Boolean>().convention(true)

    /**
     * Fail on any `JAVA_HOME` issues.
     */
    val failOnError = objects.property<Boolean>().convention(true)

    /**
     * Extra message text, if any, to show with the Gradle Doctor message. This is useful if you have a wiki page or
     * other instructions that you want to link for developers on your team if they encounter an issue.
     */
    @Suppress("CAST_NEVER_SUCCEEDS") // Cast is for overload ambiguity
    val extraMessage = objects.property<String>().convention(null as? String)
}
