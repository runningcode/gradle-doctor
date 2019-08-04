package com.osacky.doctor

import java.util.concurrent.TimeUnit

class GarbagePrinter(private val clock: Clock, private val collector: DirtyBeanCollector) : BuildStartFinishListener {

    private val startGarbageTime = collector.collect()
    private val startBuildTime = clock.upTime().toMillis()

    override fun onStart() {
    }

    override fun onFinish() {
        val endGarbageTime = collector.collect()
        val endBuildTime = clock.upTime().toMillis()

        val buildDuration = endBuildTime - startBuildTime
        val garbageDuration = endGarbageTime - startGarbageTime


        println("build took $buildDuration")
        println("garbage took $garbageDuration")
    }

    private fun Long.toMillis(): Long {
        return TimeUnit.NANOSECONDS.toMillis(this)
    }
}
