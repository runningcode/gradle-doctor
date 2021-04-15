package com.osacky.doctor

import com.osacky.doctor.internal.Clock
import com.osacky.doctor.internal.SlowNetworkPrinter.Companion.ONE_MEGABYTE
import com.osacky.doctor.internal.twoDigits
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.SourceTask
import java.io.File

class RemoteCacheEstimation(
    private val buildOperations: BuildOperations,
    private val project: Project,
    private val clock: Clock
) : BuildStartFinishListener {

    private val benchmarkBuildCache: Boolean = project.properties.containsKey("benchmarkRemoteCache")
    private val rerunSourceTasks: Boolean = project.properties.containsKey("rerunSourceTasksForBenchmark")
    private val rerunLargeOutputTasks: Boolean = project.properties.containsKey("rerunLargeOutputTasksForBenchmark")
    private var startTime: Long = -1L

    override fun onStart() {
        if (!benchmarkBuildCache) {
            return
        }
        project.gradle.addBuildListener(listener)
        // Re-run all source tasks for benchmarking purposes
        if (rerunSourceTasks || rerunLargeOutputTasks) {
            project.allprojects {
                if (rerunSourceTasks) {
                    tasks.withType(SourceTask::class.java).configureEach {
                        outputs.upToDateWhen { false }
                    }
                }
                if (rerunLargeOutputTasks) {
                    // Look up tasks by name so we don't depend on the Android Plugin.
                    // If the task has a different build type (not debug), it likely won't work here though.
                    tasks.matching { it.name == "processDebugResources" || it.name == "mergeDebugJavaResource" || it.name == "mergeDebugAssets" || it.name == "mergeDebugResources" || it.name == "bundleLibResDebug" || it.name == "packageDebugResources" || it.name == "mergeDebugNativeLibs" || it.name == "generateDebugUnitTestStubRFile" }
                        .configureEach {
                            outputs.upToDateWhen { false }
                        }
                }
            }
        }
    }

    override fun onFinish(): List<String> {
        if (!benchmarkBuildCache) {
            return emptyList()
        }
        project.gradle.removeListener(listener)

        val cacheDir = gradleLocalCacheDir()

        // For every task output hash, find the size of the corresponding compressed artifact in the build cache directory.
        val cacheSizeBytes = buildOperations.cacheKeys().sumBy {
            File(cacheDir, it.toString()).length().toInt()
        }

        if (cacheSizeBytes == 0) {
            return listOf(
                """
                = Remote Build Cache Benchmark Report =
                This build did not generate any cached artifacts.
                """.trimIndent()
            )
        }

        val endTime = clock.upTimeMillis()

        check(startTime != -1L)
        val executionTime = endTime - startTime
        val executionTimeSec = executionTime / 1000f

        val cacheSizeMB = cacheSizeBytes * 1.0f / ONE_MEGABYTE
        val minBuildCacheSpeed = (cacheSizeMB / executionTimeSec) * 1.0f

        val oneMBTime = cacheSizeMB
        val twoMBTime = cacheSizeMB / 2f
        val tenMBTime = cacheSizeMB / 10f

        val oneMBSavings = executionTimeSec - oneMBTime
        val twoMBSavings = executionTimeSec - twoMBTime
        val tenMBSavings = executionTimeSec - tenMBTime

        return listOf(
            """
            = Remote Build Cache Benchmark Report =
            Forced re-execution of ${buildOperations.tasksRan()} tasks in order to calculate local execution duration.
            Executed tasks created compressed artifacts of size ${twoDigits.format(cacheSizeMB)} MB
            Total task execution time was ${twoDigits.format(executionTimeSec)} s

            In order for a remote build cache to save you time, you would need a connection speed to your node of at least ${twoDigits.format(minBuildCacheSpeed)} MB/s.
            Check a build scan to see your connection speed to the build cache node.
            Build cache node throughput may be different than your internet connection speed.

            A 1 MB/s connection would save you ${twoDigits.format(oneMBSavings)} s.
            A 2 MB/s connection would save you ${twoDigits.format(twoMBSavings)} s.
            A 10 MB/s connection would save you ${twoDigits.format(tenMBSavings)} s.

            Note: This is an estimate. Real world performance may vary. This estimate does not take in to account time spent decompressing cached artifacts or roundtrip communication time to the cache node.
            """.trimIndent()
        )
    }

    private fun gradleLocalCacheDir(): File {
        val gradleInternalCacheDir = (project.gradle as GradleInternal).settings.buildCache.local.directory
        return if (gradleInternalCacheDir != null) {
            when (gradleInternalCacheDir) {
                is File -> {
                    gradleInternalCacheDir
                }
                is String -> {
                    File(gradleInternalCacheDir)
                }
                else -> {
                    throw IllegalStateException("Unexpected type for $gradleInternalCacheDir")
                }
            }
        } else {
            File(project.gradle.gradleUserHomeDir, "caches/build-cache-1")
        }
    }

    private val listener = object : BuildListener {
        override fun settingsEvaluated(settings: Settings) {
        }

        override fun buildFinished(result: BuildResult) {
        }

        override fun projectsLoaded(gradle: Gradle) {
        }

        fun buildStarted(gradle: Gradle) {
        }

        override fun projectsEvaluated(gradle: Gradle) {
            // Configuration time is complete. We only want to measure task execution time.
            startTime = clock.upTimeMillis()
        }
    }
}
