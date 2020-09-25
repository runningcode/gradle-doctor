package com.osacky.doctor

import com.osacky.doctor.internal.Clock
import com.osacky.doctor.internal.DirtyBeanCollector
import java.text.NumberFormat

class GarbagePrinter(
    private val clock: Clock,
    private val collector: DirtyBeanCollector,
    private val extension: DoctorExtension
) : BuildStartFinishListener, HasBuildScanTag {

    private val startGarbageTime = collector.collect()
    private val startBuildTime = clock.upTimeMillis()
    private val formatter = NumberFormat.getPercentInstance()
    private val warningThreshold = 10 * 1000

    override fun onStart() {
    }

    override fun onFinish(): List<String> {
        val endGarbageTime = collector.collect()
        val endBuildTime = clock.upTimeMillis()

        val buildDuration = endBuildTime - startBuildTime
        val garbageDuration = endGarbageTime - startGarbageTime

        val percentGarbageCollecting = (garbageDuration * 1f / buildDuration)
        if (buildDuration > warningThreshold && percentGarbageCollecting > extension.GCWarningThreshold.get()) {
            val message =
                """
                This build spent ${formatter.format(percentGarbageCollecting)} garbage collecting.
                If this is the first build with this Daemon, it likely means that this build needs more heap space.
                Otherwise, if this is happening after several builds it could indicate a memory leak.
                For a quick fix, restart this Gradle daemon. ./gradlew --stop
                """.trimIndent()
            return listOf(message)
        }
        return emptyList()
    }

    override fun getTag(): String = "high-gc"
}
