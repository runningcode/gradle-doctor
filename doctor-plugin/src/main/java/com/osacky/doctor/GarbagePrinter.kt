package com.osacky.doctor

import com.osacky.doctor.internal.Clock
import com.osacky.doctor.internal.DirtyBeanCollector
import com.osacky.doctor.internal.Finish
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

class GarbagePrinter(
    private val clock: Clock,
    private val collector: DirtyBeanCollector,
    private val extension: DoctorExtension
) : BuildStartFinishListener {

    private val startGarbageTime = collector.collect()
    private val startBuildTime = clock.upTime().toMillis()
    private val formatter = NumberFormat.getPercentInstance()

    override fun onStart() {
    }

    override fun onFinish(): Finish {
        val endGarbageTime = collector.collect()
        val endBuildTime = clock.upTime().toMillis()

        val buildDuration = endBuildTime - startBuildTime
        val garbageDuration = endGarbageTime - startGarbageTime

        val percentGarbageCollecting = (garbageDuration * 1f / buildDuration)
        if (percentGarbageCollecting > extension.GCWarningThreshold) {
            val message = """
                This build spent ${formatter.format(percentGarbageCollecting)} garbage collecting.
                If this is the first build with this Daemon, it likely means that this build needs more heap space.
                Otherwise, if this is happening after several builds it could indicate a memory leak.
                For a quick fix, restart this Gradle daemon. ./gradlew --stop
            """.trimIndent()
            return Finish.FinishMessage(message)
        }
        return Finish.None
    }

    private fun Long.toMillis(): Long {
        return TimeUnit.NANOSECONDS.toMillis(this)
    }
}
