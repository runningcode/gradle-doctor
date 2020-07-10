package com.osacky.doctor

import com.osacky.doctor.internal.Clock
import com.osacky.doctor.internal.DaemonCheck
import com.osacky.doctor.internal.DirtyBeanCollector
import com.osacky.doctor.internal.Finish
import com.osacky.doctor.internal.IntervalMeasurer
import com.osacky.doctor.internal.PillBoxPrinter
import com.osacky.doctor.internal.SystemClock
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.util.GradleVersion
import org.gradle.util.VersionNumber
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

class DoctorPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        ensureMinimumSupportedGradleVersion()
        ensureAppliedInProjectRoot(target)

        val extension = target.extensions.create<DoctorExtension>("doctor")

        val clock: Clock = SystemClock()
        val intervalMeasurer = IntervalMeasurer()
        val pillBoxPrinter = PillBoxPrinter(target.logger)
        val daemonChecker = BuildDaemonChecker(extension, DaemonCheck(), pillBoxPrinter)
        val javaHomeCheck = JavaHomeCheck(extension, pillBoxPrinter)
        val garbagePrinter = GarbagePrinter(clock, DirtyBeanCollector(), extension)
        val operations = BuildOperations(target.gradle)
        val javaAnnotationTime = JavaAnnotationTime(operations, extension, target.buildscript.configurations)
        val downloadSpeedMeasurer = DownloadSpeedMeasurer(operations, extension, intervalMeasurer)
        val buildCacheConnectionMeasurer = BuildCacheConnectionMeasurer(operations, extension, intervalMeasurer)
        val buildCacheKey = RemoteCacheEstimation(operations, target, clock)
        val list = listOf(daemonChecker, javaHomeCheck, garbagePrinter, javaAnnotationTime, downloadSpeedMeasurer, buildCacheConnectionMeasurer, buildCacheKey)
        garbagePrinter.onStart()
        javaAnnotationTime.onStart()
        downloadSpeedMeasurer.onStart()
        buildCacheConnectionMeasurer.onStart()
        buildCacheKey.onStart()
        target.afterEvaluate {
            daemonChecker.onStart()
            javaHomeCheck.onStart()
        }

        target.gradle.buildFinished {
            val thingsToPrint = list.map { it.onFinish() }.filterIsInstance(Finish.FinishMessage::class.java)
            if (thingsToPrint.isEmpty()) {
                return@buildFinished
            }

            pillBoxPrinter.writePrescription(thingsToPrint.map { it.message })
        }

        val appPluginProjects = mutableSetOf<Project>()

        target.subprojects project@{
            afterEvaluate {
                if (extension.failOnEmptyDirectories) {
                    // Fail build if empty directories are found. These cause build cache misses and should be ignored by Gradle.
                    tasks.withType(SourceTask::class.java).configureEach {
                        doFirst {
                            source.visit {
                                if (file.isDirectory && file.listFiles().isEmpty()) {
                                    throw IllegalStateException("Empty src dir found. This causes build cache misses. Run the following command to fix it.\nrmdir ${file.absolutePath}")
                                }
                            }
                        }
                    }
                }
                // Ensure we are not caching any test tasks. Tests may not declare all inputs properly or depend on things like the date and caching them can lead to dangerous false positives.
                if (!extension.enableTestCaching) {
                    tasks.withType(Test::class.java).configureEach {
                        outputs.upToDateWhen { false }
                    }
                }
            }
            plugins.whenPluginAdded plugin@{
                if (this.javaClass.name == "com.android.build.gradle.AppPlugin") {
                    appPluginProjects.add(this@project)
                }
                if (this.javaClass.name == "org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin") {
                    if (VersionNumber.parse(this@project.getKotlinPluginVersion()!!).baseVersion >= VersionNumber.parse("1.3.50")) {
                        val kapt3Extension = this@project.extensions.findByType(KaptExtension::class.java)!!
                        kapt3Extension.showProcessorTimings = true
                    }
                }
            }
        }

        target.gradle.taskGraph.whenReady {
            // If there is only one application plugin, we don't need to check that we're assembling all the applications.
            if (appPluginProjects.size <= 1 || extension.allowBuildingAllAndroidAppsSimultaneously) {
                return@whenReady
            }
            val assembleTasksInAndroidAppProjects = allTasks
                // Find executing tasks which are in Android AppPlugin Projects and contain the word assemble in the name.
                .filter { appPluginProjects.contains(it.project) && it.name.contains("assemble") || it.name.contains("install") }
            val projectsWithAssembleTasks = assembleTasksInAndroidAppProjects.map { it.project }.toSet()
            // Check if we have at least one assemble task in every project which has the application plugin.
            if (projectsWithAssembleTasks.containsAll(appPluginProjects)) {
                val errorMessage =
                    """
                    |Did you really mean to run all these? $assembleTasksInAndroidAppProjects
                    |Maybe you just meant to assemble/install one of them? In that case, you can try
                    |  ./gradlew ${assembleTasksInAndroidAppProjects[0].project.name}:${assembleTasksInAndroidAppProjects[0].name}
                    |Or did you hit "build" in the IDE (Green Hammer)? Did you know that assembles all the code in the entire project?
                    |Next time try "Sync Project with Gradle Files" (Gradle Elephant with Arrow).
                """.trimMargin("|")
                throw GradleException(pillBoxPrinter.createPill(errorMessage))
            }
        }
    }

    private fun ensureAppliedInProjectRoot(target: Project) {
        if (target.parent != null) {
            throw GradleException("Gradle Doctor must be applied in the project root.")
        }
    }

    private fun ensureMinimumSupportedGradleVersion() {
        if (GradleVersion.current() < GradleVersion.version("5.1")) {
            throw GradleException("Must be using Gradle Version 5.1 in order to use DoctorPlugin. Current Gradle Version is ${GradleVersion.current()}")
        }
    }
}
