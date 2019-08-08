package com.osacky.doctor

import com.osacky.doctor.internal.Clock
import com.osacky.doctor.internal.DirtyBeanCollector
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

    override fun onFinish() {
        val endGarbageTime = collector.collect()
        val endBuildTime = clock.upTime().toMillis()

        val buildDuration = endBuildTime - startBuildTime
        val garbageDuration = endGarbageTime - startGarbageTime

        val percentGarbageCollecting = (garbageDuration * 1f / buildDuration)
        if (percentGarbageCollecting > extension.GCWarningThreshold) {
            println("This build spent ${formatter.format(percentGarbageCollecting)} garbage collecting!")
        }
    }

    private fun Long.toMillis(): Long {
        return TimeUnit.NANOSECONDS.toMillis(this)
    }
}
