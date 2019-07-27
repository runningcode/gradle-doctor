package com.osacky.doctor

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.testing.Test
import org.gradle.util.GradleVersion

class DoctorPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        ensureMinimumSupportedGradleVersion()

        val currentlyActiveGradleDaemons = DaemonCheck().numberOfDaemons()
        if (currentlyActiveGradleDaemons > 1) {
            println("there are currently more than one Gradle Daemons. Found $currentlyActiveGradleDaemons")
        }

        val garbagePrinter = GarbagePrinter(SystemClock(), DirtyBeanCollector())
        val operations = BuildOperations(target.gradle)
        val javaAnnotationTime = JavaAnnotationTime(operations)
        val deprecationWarningPrinter = DeprecationWarningPrinter(operations)
        deprecationWarningPrinter.start()

        target.gradle.buildFinished {
            garbagePrinter.onFinish()
            javaAnnotationTime.onFinished()
            if (target.hasProperty("org.gradle.warning.mode") && target.property("org.gradle.warning.mode") == "all") {
                deprecationWarningPrinter.buildFinished()
            }
        }

        val appPluginProjects = mutableSetOf<Project>()

        target.subprojects project@ {
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
            // Ensure we are not caching any test tasks. Tests may not declare all inputs properly or depend on things like the date and caching them can lead to dangerous false positives.
            tasks.withType(Test::class.java).configureEach {
                outputs.upToDateWhen { false }
            }
            plugins.whenPluginAdded plugin@ {
                if (this.javaClass.name == "com.android.build.gradle.AppPlugin") {
                    appPluginProjects.add(this@project)
                }
            }
        }

        target.gradle.taskGraph.whenReady {
            // If there is only one application plugin, we don't need to check that we're assembling all the applications.
            if (appPluginProjects.size <= 1) {
                return@whenReady
            }
            val assembleTasksInAndroidAppProjects = allTasks
                    // Find executing tasks which are in Android AppPlugin Projects and contain the word assemble in the name.
                    .filter { appPluginProjects.contains(it.project) && it.name.contains("assemble")}
            val projectsWithAssembleTasks = assembleTasksInAndroidAppProjects.map { it.project }.toSet()
            // Check if we have at least one assemble task in every project which has the application plugin.
            if (projectsWithAssembleTasks.containsAll(appPluginProjects)) {
                val errorMessage = """
                    |Did you really mean to run all these? $assembleTasksInAndroidAppProjects
                    |Maybe you just meant to assemble one of them? In that case, you can try
                    |  ./gradlew ${assembleTasksInAndroidAppProjects[0].project.name}:${assembleTasksInAndroidAppProjects[0].name}
                    |Or did you hit "build" in the IDE (Green Hammer)? Did you know that assembles all the code in the entire project?
                    |Next time try "Sync Project with Gradle Files" (Gradle Elephant with Arrow).
                """.trimMargin("|")
                throw GradleException(errorMessage)
            }
        }
    }

    private fun ensureMinimumSupportedGradleVersion() {
        if (GradleVersion.current() < GradleVersion.version("5.1")) {
            throw GradleException("Must be using Gradle Version 5.1 in order to use DoctorPlugin. Current Gradle Version is ${GradleVersion.current()}")
        }
    }

}