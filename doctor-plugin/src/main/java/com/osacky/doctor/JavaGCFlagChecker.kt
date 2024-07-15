package com.osacky.doctor

import com.osacky.doctor.internal.PillBoxPrinter
import org.gradle.api.GradleException
import java.lang.management.ManagementFactory

/**
 * Starting with Java 9, G1 is the new GC. For building software such as with Gradle, the parallel GC is faster.
 */
class JavaGCFlagChecker(
    private val pillBoxPrinter: PillBoxPrinter,
    private val extension: DoctorExtension,
) : BuildStartFinishListener {
    private val parallelGCFlag = "-XX:+UseParallelGC"

    override fun onStart() {
        if (!extension.warnWhenNotUsingParallelGC.get()) {
            return
        }
        if (getJavaVersion() > 9) {
            if (!ManagementFactory.getRuntimeMXBean().inputArguments.contains(parallelGCFlag)) {
                throw GradleException(
                    pillBoxPrinter.createPill(
                        """
                        For faster builds, use the parallel GC.
                        Add $parallelGCFlag to the org.gradle.jvmargs
                        """.trimIndent(),
                    ),
                )
            }
        }
    }

    override fun onFinish(): List<String> = emptyList()

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
