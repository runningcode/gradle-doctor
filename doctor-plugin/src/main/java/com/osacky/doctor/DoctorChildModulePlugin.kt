package com.osacky.doctor

import com.osacky.doctor.internal.farthestEmptyParent
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.testing.Test
import org.gradle.util.GradleVersion

class DoctorChildModulePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.getDoctorExtension()
        target.tasks.withType(SourceTask::class.java).configureEach {
            if (!gradleIgnoresEmptyDirectories() && extension.failOnEmptyDirectories.get()) {
                // Fail build if empty directories are found. These cause build cache misses and should be ignored by Gradle.
                doFirst {
                    source.visit {
                        if (file.isDirectory && file.listFiles().isEmpty()) {
                            val farthestEmptyParent = file.farthestEmptyParent()
                            throw IllegalStateException(
                                "Empty src dir(s) found. This causes build cache misses. Run the following command to fix it.\n" +
                                        "rmdir ${farthestEmptyParent.absolutePath}",
                            )
                        }
                    }
                }
            }
        }
        // Ensure we are not caching any test tasks. Tests may not declare all inputs properly or depend on things like the date and caching them can lead to dangerous false positives.
        target.tasks.withType(Test::class.java).configureEach {
            if (!extension.enableTestCaching.get()) {
                outputs.upToDateWhen { false }
            }
        }
        target.plugins.whenPluginAdded plugin@{
            if (this.javaClass.name == "com.android.build.gradle.AppPlugin") {
                target.getAppProjectCollectorBuildService().addProject(target)
            }
        }
    }

    /**
     * Gradle now ignores empty directories starting in 6.8
     * https://docs.gradle.org/6.8-rc-1/release-notes.html#performance-improvements
     **/
    private fun gradleIgnoresEmptyDirectories(): Boolean = GradleVersion.current() >= GradleVersion.version("6.8-rc-1")

}