package com.osacky.doctor

import com.osacky.doctor.internal.PillBoxPrinter
import org.gradle.api.GradleException
import java.lang.management.ManagementFactory

/**
 * Certain combinations of Java versions and their default GC are faster than others.
 * For Java versions 9-16, the default is G1GC, but Parallel GC is faster.
 * For Java versions 17+, G1GC is faster.
 */
class OptimalGCFlagChecker(
    private val pillBoxPrinter: PillBoxPrinter,
    private val extension: DoctorExtension
) : BuildStartFinishListener {

    companion object {
        private const val PARALLEL_GC_FLAG = "-XX:+UseParallelGC"
        private const val G1_GC_FLAG = "-XX:+UseG1GC"
    }

    override fun onStart() {
        if (!extension.failWhenNotUsingOptimalGC.get()) {
            return
        }
        getJavaVersion().let {
            when {
                it >= 17 -> checkForG1gc()
                it in 9..16 -> checkForParallelGc()
                else -> { }
            }
        }
    }

    private fun checkForG1gc() {
        if (!ManagementFactory.getRuntimeMXBean().inputArguments.contains(G1_GC_FLAG)) {
            throw GradleException(
                pillBoxPrinter.createPill(
                    """
               For faster builds, use the G1 GC.
               Add $G1_GC_FLAG to the org.gradle.jvmargs
                    """.trimIndent()
                )
            )
        }
    }

    private fun checkForParallelGc() {
        if (!ManagementFactory.getRuntimeMXBean().inputArguments.contains(PARALLEL_GC_FLAG)) {
            throw GradleException(
                pillBoxPrinter.createPill(
                    """
               For faster builds, use the parallel GC.
               Add $PARALLEL_GC_FLAG to the org.gradle.jvmargs
                    """.trimIndent()
                )
            )
        }
    }

    override fun onFinish(): List<String> {
        return emptyList()
    }

    /**
     * Returns the Java version as an Int in a backward compatible way.
     * There is no straightforward Java version API in Java 8.
     */
    private fun getJavaVersion(): Int {
        var version = System.getProperty("java.specification.version")
        if (version.startsWith("1.")) {
            version = version.substring(2, 3)
        } else {
            val dot = version.indexOf(".")
            if (dot != -1) {
                version = version.substring(0, dot)
            }
        }
        return version.toInt()
    }
}
